/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.deegree.i18n.Messages
 */
package org.deegree.ogcwebservices.wfs.capabilities;

import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.Set;
import org.deegree.i18n.Messages;

public class Operation {
    public static final String INSERT = "Insert";
    public static final String UPDATE = "Update";
    public static final String DELETE = "Delete";
    public static final String QUERY = "Query";
    public static final String LOCK = "Lock";
    public static final String GET_GML_OBJECT = "GetGMLObject";
    private static final Set<String> VALID_OPERATIONS = new HashSet<String>();
    private String operation;

    static {
        VALID_OPERATIONS.add(INSERT);
        VALID_OPERATIONS.add(UPDATE);
        VALID_OPERATIONS.add(DELETE);
        VALID_OPERATIONS.add(QUERY);
        VALID_OPERATIONS.add(LOCK);
        VALID_OPERATIONS.add(GET_GML_OBJECT);
    }

    public String getOperation() {
        return this.operation;
    }

    public Operation(String operation) throws InvalidParameterException {
        String opString = operation;
        if (opString.startsWith("wfs:")) {
            opString = opString.replaceFirst("wfs:", "");
        }
        if (!VALID_OPERATIONS.contains(opString)) {
            String msg = Messages.getMessage((String)"WFS_INVALID_OPERATION_TYPE", (Object[])new Object[]{operation});
            throw new InvalidParameterException(msg);
        }
        this.operation = opString;
    }
}

