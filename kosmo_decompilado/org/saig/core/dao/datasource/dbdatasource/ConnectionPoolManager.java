/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.dao.datasource.dbdatasource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;
import org.apache.log4j.Logger;

public class ConnectionPoolManager {
    private static final Logger LOGGER = Logger.getLogger(ConnectionPoolManager.class);
    private ConnectionPoolDataSource dataSource;
    private int maxConnections;
    private int timeout;
    private Semaphore semaphore;
    private Queue<PooledConnection> recycledConnections;
    private int activeConnections;
    private PoolConnectionEventListener poolConnectionEventListener;
    private boolean isDisposed;

    public ConnectionPoolManager(ConnectionPoolDataSource dataSource, int maxConnections) {
        this(dataSource, maxConnections, 60);
    }

    public ConnectionPoolManager(ConnectionPoolDataSource dataSource, int maxConnections, int timeout) {
        this.dataSource = dataSource;
        this.maxConnections = maxConnections;
        this.timeout = timeout;
        if (maxConnections < 1) {
            throw new IllegalArgumentException("Invalid maxConnections value.");
        }
        this.semaphore = new Semaphore(maxConnections, true);
        this.recycledConnections = new LinkedList<PooledConnection>();
        this.poolConnectionEventListener = new PoolConnectionEventListener();
    }

    public synchronized void dispose() throws SQLException {
        if (this.isDisposed) {
            return;
        }
        this.isDisposed = true;
        SQLException e = null;
        while (!this.recycledConnections.isEmpty()) {
            PooledConnection pconn = this.recycledConnections.remove();
            try {
                pconn.close();
            }
            catch (SQLException e2) {
                if (e != null) continue;
                e = e2;
            }
        }
        if (e != null) {
            throw e;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Connection getConnection() throws SQLException {
        ConnectionPoolManager connectionPoolManager = this;
        synchronized (connectionPoolManager) {
            if (this.isDisposed) {
                throw new IllegalStateException("Connection pool has been disposed.");
            }
        }
        try {
            if (!this.semaphore.tryAcquire(this.timeout, TimeUnit.SECONDS)) {
                throw new TimeoutException();
            }
        }
        catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while waiting for a database connection.", e);
        }
        boolean ok = false;
        try {
            Connection conn = this.getConnection2();
            ok = true;
            Connection connection = conn;
            return connection;
        }
        finally {
            if (!ok) {
                this.semaphore.release();
            }
        }
    }

    private synchronized Connection getConnection2() throws SQLException {
        if (this.isDisposed) {
            throw new IllegalStateException("Connection pool has been disposed.");
        }
        PooledConnection pconn = !this.recycledConnections.isEmpty() ? this.recycledConnections.remove() : this.dataSource.getPooledConnection();
        Connection conn = pconn.getConnection();
        ++this.activeConnections;
        pconn.addConnectionEventListener(this.poolConnectionEventListener);
        this.assertInnerState();
        return conn;
    }

    private synchronized void recycleConnection(PooledConnection pconn) {
        if (this.isDisposed) {
            this.disposeConnection(pconn);
            return;
        }
        if (this.activeConnections <= 0) {
            throw new AssertionError();
        }
        --this.activeConnections;
        this.semaphore.release();
        this.recycledConnections.add(pconn);
        this.assertInnerState();
    }

    private synchronized void disposeConnection(PooledConnection pconn) {
        if (this.activeConnections <= 0) {
            throw new AssertionError();
        }
        --this.activeConnections;
        this.semaphore.release();
        this.closeConnectionNoEx(pconn);
        this.assertInnerState();
    }

    private void closeConnectionNoEx(PooledConnection pconn) {
        try {
            pconn.close();
        }
        catch (SQLException e) {
            LOGGER.error((Object)("Error while closing database connection: " + e.toString()));
        }
    }

    private void assertInnerState() {
        if (this.activeConnections < 0) {
            throw new AssertionError();
        }
        if (this.activeConnections + this.recycledConnections.size() > this.maxConnections) {
            throw new AssertionError();
        }
        if (this.activeConnections + this.semaphore.availablePermits() > this.maxConnections) {
            throw new AssertionError();
        }
    }

    public synchronized int getActiveConnections() {
        return this.activeConnections;
    }

    private class PoolConnectionEventListener
    implements ConnectionEventListener {
        private PoolConnectionEventListener() {
        }

        @Override
        public void connectionClosed(ConnectionEvent event) {
            PooledConnection pconn = (PooledConnection)event.getSource();
            pconn.removeConnectionEventListener(this);
            ConnectionPoolManager.this.recycleConnection(pconn);
        }

        @Override
        public void connectionErrorOccurred(ConnectionEvent event) {
            PooledConnection pconn = (PooledConnection)event.getSource();
            pconn.removeConnectionEventListener(this);
            ConnectionPoolManager.this.disposeConnection(pconn);
        }
    }

    public static class TimeoutException
    extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public TimeoutException() {
            super("Timeout while waiting for a free database connection.");
        }
    }
}

