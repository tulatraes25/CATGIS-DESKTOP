/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter.parser;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Vector;
import org.saig.core.filter.parser.ExpressionParserConstants;
import org.saig.core.filter.parser.ExpressionParserTokenManager;
import org.saig.core.filter.parser.ExpressionParserTreeConstants;
import org.saig.core.filter.parser.JJTExpressionParserState;
import org.saig.core.filter.parser.Node;
import org.saig.core.filter.parser.ParseException;
import org.saig.core.filter.parser.SimpleCharStream;
import org.saig.core.filter.parser.SimpleNode;
import org.saig.core.filter.parser.Token;

public class ExpressionParser
implements ExpressionParserTreeConstants,
ExpressionParserConstants {
    protected JJTExpressionParserState jjtree = new JJTExpressionParserState();
    public ExpressionParserTokenManager token_source;
    SimpleCharStream jj_input_stream;
    public Token token;
    public Token jj_nt;
    private int jj_ntk;
    private Token jj_scanpos;
    private Token jj_lastpos;
    private int jj_la;
    public boolean lookingAhead = false;
    private boolean jj_semLA;
    private int jj_gen;
    private final int[] jj_la1 = new int[28];
    private final int[] jj_la1_0;
    private final int[] jj_la1_1;
    private final JJCalls[] jj_2_rtns;
    private boolean jj_rescan;
    private int jj_gc;
    private Vector jj_expentries;
    private int[] jj_expentry;
    private int jj_kind;
    private int[] jj_lasttokens;
    private int jj_endpos;

    public void jjtreeOpenNodeScope(Node n) throws ParseException {
    }

    public void jjtreeCloseNodeScope(Node n) throws ParseException {
    }

    public static void main(String[] args) throws ParseException {
        ExpressionParser parser = new ExpressionParser(new StringReader("totalcons >= 500"));
        try {
            SimpleNode n = (SimpleNode)parser.CompilationUnit();
            n.dump("");
            System.out.println("Thank you.");
        }
        catch (Exception e) {
            System.out.println("Oops.");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public final Node CompilationUnit() throws ParseException {
        block3: while (true) {
            switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                case 8: 
                case 9: 
                case 14: 
                case 21: 
                case 22: 
                case 23: 
                case 24: 
                case 25: 
                case 26: 
                case 27: 
                case 28: 
                case 29: 
                case 30: 
                case 32: 
                case 34: 
                case 35: 
                case 36: {
                    break;
                }
                default: {
                    this.jj_la1[0] = this.jj_gen;
                    break block3;
                }
            }
            this.Expression();
        }
        this.jj_consume_token(0);
        return this.jjtree.rootNode();
    }

    public final void Expression() throws ParseException {
        this.OrExpression();
    }

    public final void OrExpression() throws ParseException {
        this.AndExpression();
        block8: while (true) {
            switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                case 13: {
                    break;
                }
                default: {
                    this.jj_la1[1] = this.jj_gen;
                    break block8;
                }
            }
            this.jj_consume_token(13);
            SimpleNode jjtn001 = new SimpleNode(1);
            boolean jjtc001 = true;
            this.jjtree.openNodeScope(jjtn001);
            this.jjtreeOpenNodeScope(jjtn001);
            try {
                try {
                    this.AndExpression();
                    continue;
                }
                catch (Throwable jjte001) {
                    if (jjtc001) {
                        this.jjtree.clearNodeScope(jjtn001);
                        jjtc001 = false;
                    } else {
                        this.jjtree.popNode();
                    }
                    if (jjte001 instanceof RuntimeException) {
                        throw (RuntimeException)jjte001;
                    }
                    if (jjte001 instanceof ParseException) {
                        throw (ParseException)jjte001;
                    }
                    throw (Error)jjte001;
                }
            }
            finally {
                if (!jjtc001) continue;
                this.jjtree.closeNodeScope((Node)jjtn001, 2);
                this.jjtreeCloseNodeScope(jjtn001);
                continue;
            }
            break;
        }
    }

    public final void AndExpression() throws ParseException {
        this.EqualityExpression();
        block8: while (true) {
            switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                case 12: {
                    break;
                }
                default: {
                    this.jj_la1[2] = this.jj_gen;
                    break block8;
                }
            }
            this.jj_consume_token(12);
            SimpleNode jjtn001 = new SimpleNode(2);
            boolean jjtc001 = true;
            this.jjtree.openNodeScope(jjtn001);
            this.jjtreeOpenNodeScope(jjtn001);
            try {
                try {
                    this.EqualityExpression();
                    continue;
                }
                catch (Throwable jjte001) {
                    if (jjtc001) {
                        this.jjtree.clearNodeScope(jjtn001);
                        jjtc001 = false;
                    } else {
                        this.jjtree.popNode();
                    }
                    if (jjte001 instanceof RuntimeException) {
                        throw (RuntimeException)jjte001;
                    }
                    if (jjte001 instanceof ParseException) {
                        throw (ParseException)jjte001;
                    }
                    throw (Error)jjte001;
                }
            }
            finally {
                if (!jjtc001) continue;
                this.jjtree.closeNodeScope((Node)jjtn001, 2);
                this.jjtreeCloseNodeScope(jjtn001);
                continue;
            }
            break;
        }
    }

    public final void EqualityExpression() throws ParseException {
        block26: {
            this.RelationalExpression();
            block17: while (true) {
                switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                    case 15: 
                    case 16: {
                        break;
                    }
                    default: {
                        this.jj_la1[3] = this.jj_gen;
                        break block26;
                    }
                }
                switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                    case 15: {
                        this.jj_consume_token(15);
                        SimpleNode jjtn001 = new SimpleNode(3);
                        boolean jjtc001 = true;
                        this.jjtree.openNodeScope(jjtn001);
                        this.jjtreeOpenNodeScope(jjtn001);
                        try {
                            try {
                                this.RelationalExpression();
                                continue block17;
                            }
                            catch (Throwable jjte001) {
                                if (jjtc001) {
                                    this.jjtree.clearNodeScope(jjtn001);
                                    jjtc001 = false;
                                } else {
                                    this.jjtree.popNode();
                                }
                                if (jjte001 instanceof RuntimeException) {
                                    throw (RuntimeException)jjte001;
                                }
                                if (jjte001 instanceof ParseException) {
                                    throw (ParseException)jjte001;
                                }
                                throw (Error)jjte001;
                            }
                        }
                        finally {
                            if (!jjtc001) continue block17;
                            this.jjtree.closeNodeScope((Node)jjtn001, 2);
                            this.jjtreeCloseNodeScope(jjtn001);
                            continue block17;
                        }
                    }
                    case 16: {
                        this.jj_consume_token(16);
                        SimpleNode jjtn002 = new SimpleNode(4);
                        boolean jjtc002 = true;
                        this.jjtree.openNodeScope(jjtn002);
                        this.jjtreeOpenNodeScope(jjtn002);
                        try {
                            try {
                                this.RelationalExpression();
                                continue block17;
                            }
                            catch (Throwable jjte002) {
                                if (jjtc002) {
                                    this.jjtree.clearNodeScope(jjtn002);
                                    jjtc002 = false;
                                } else {
                                    this.jjtree.popNode();
                                }
                                if (jjte002 instanceof RuntimeException) {
                                    throw (RuntimeException)jjte002;
                                }
                                if (jjte002 instanceof ParseException) {
                                    throw (ParseException)jjte002;
                                }
                                throw (Error)jjte002;
                            }
                        }
                        finally {
                            if (!jjtc002) continue block17;
                            this.jjtree.closeNodeScope((Node)jjtn002, 2);
                            this.jjtreeCloseNodeScope(jjtn002);
                            continue block17;
                        }
                    }
                }
                break;
            }
            this.jj_la1[4] = this.jj_gen;
            this.jj_consume_token(-1);
            throw new ParseException();
        }
    }

    public final void RelationalExpression() throws ParseException {
        block57: {
            this.AdditiveExpression();
            if (this.jj_2_1(3)) {
                this.jj_consume_token(18);
                this.AdditiveExpression();
                this.jj_consume_token(18);
                SimpleNode jjtn001 = new SimpleNode(5);
                boolean jjtc001 = true;
                this.jjtree.openNodeScope(jjtn001);
                this.jjtreeOpenNodeScope(jjtn001);
                try {
                    try {
                        this.AdditiveExpression();
                        break block57;
                    }
                    catch (Throwable jjte001) {
                        if (jjtc001) {
                            this.jjtree.clearNodeScope(jjtn001);
                            jjtc001 = false;
                        } else {
                            this.jjtree.popNode();
                        }
                        if (jjte001 instanceof RuntimeException) {
                            throw (RuntimeException)jjte001;
                        }
                        if (jjte001 instanceof ParseException) {
                            throw (ParseException)jjte001;
                        }
                        throw (Error)jjte001;
                    }
                }
                finally {
                    if (jjtc001) {
                        this.jjtree.closeNodeScope((Node)jjtn001, 3);
                        this.jjtreeCloseNodeScope(jjtn001);
                    }
                }
            }
            block34: while (true) {
                switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                    case 17: 
                    case 18: 
                    case 19: 
                    case 20: {
                        break;
                    }
                    default: {
                        this.jj_la1[5] = this.jj_gen;
                        break block57;
                    }
                }
                switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                    case 18: {
                        this.jj_consume_token(18);
                        SimpleNode jjtn002 = new SimpleNode(6);
                        boolean jjtc002 = true;
                        this.jjtree.openNodeScope(jjtn002);
                        this.jjtreeOpenNodeScope(jjtn002);
                        try {
                            try {
                                this.AdditiveExpression();
                                continue block34;
                            }
                            catch (Throwable jjte002) {
                                if (jjtc002) {
                                    this.jjtree.clearNodeScope(jjtn002);
                                    jjtc002 = false;
                                } else {
                                    this.jjtree.popNode();
                                }
                                if (jjte002 instanceof RuntimeException) {
                                    throw (RuntimeException)jjte002;
                                }
                                if (jjte002 instanceof ParseException) {
                                    throw (ParseException)jjte002;
                                }
                                throw (Error)jjte002;
                            }
                        }
                        finally {
                            if (!jjtc002) continue block34;
                            this.jjtree.closeNodeScope((Node)jjtn002, 2);
                            this.jjtreeCloseNodeScope(jjtn002);
                            continue block34;
                        }
                    }
                    case 17: {
                        this.jj_consume_token(17);
                        SimpleNode jjtn003 = new SimpleNode(7);
                        boolean jjtc003 = true;
                        this.jjtree.openNodeScope(jjtn003);
                        this.jjtreeOpenNodeScope(jjtn003);
                        try {
                            try {
                                this.AdditiveExpression();
                                continue block34;
                            }
                            catch (Throwable jjte003) {
                                if (jjtc003) {
                                    this.jjtree.clearNodeScope(jjtn003);
                                    jjtc003 = false;
                                } else {
                                    this.jjtree.popNode();
                                }
                                if (jjte003 instanceof RuntimeException) {
                                    throw (RuntimeException)jjte003;
                                }
                                if (jjte003 instanceof ParseException) {
                                    throw (ParseException)jjte003;
                                }
                                throw (Error)jjte003;
                            }
                        }
                        finally {
                            if (!jjtc003) continue block34;
                            this.jjtree.closeNodeScope((Node)jjtn003, 2);
                            this.jjtreeCloseNodeScope(jjtn003);
                            continue block34;
                        }
                    }
                    case 20: {
                        this.jj_consume_token(20);
                        SimpleNode jjtn004 = new SimpleNode(8);
                        boolean jjtc004 = true;
                        this.jjtree.openNodeScope(jjtn004);
                        this.jjtreeOpenNodeScope(jjtn004);
                        try {
                            try {
                                this.AdditiveExpression();
                                continue block34;
                            }
                            catch (Throwable jjte004) {
                                if (jjtc004) {
                                    this.jjtree.clearNodeScope(jjtn004);
                                    jjtc004 = false;
                                } else {
                                    this.jjtree.popNode();
                                }
                                if (jjte004 instanceof RuntimeException) {
                                    throw (RuntimeException)jjte004;
                                }
                                if (jjte004 instanceof ParseException) {
                                    throw (ParseException)jjte004;
                                }
                                throw (Error)jjte004;
                            }
                        }
                        finally {
                            if (!jjtc004) continue block34;
                            this.jjtree.closeNodeScope((Node)jjtn004, 2);
                            this.jjtreeCloseNodeScope(jjtn004);
                            continue block34;
                        }
                    }
                    case 19: {
                        this.jj_consume_token(19);
                        SimpleNode jjtn005 = new SimpleNode(9);
                        boolean jjtc005 = true;
                        this.jjtree.openNodeScope(jjtn005);
                        this.jjtreeOpenNodeScope(jjtn005);
                        try {
                            try {
                                this.AdditiveExpression();
                                continue block34;
                            }
                            catch (Throwable jjte005) {
                                if (jjtc005) {
                                    this.jjtree.clearNodeScope(jjtn005);
                                    jjtc005 = false;
                                } else {
                                    this.jjtree.popNode();
                                }
                                if (jjte005 instanceof RuntimeException) {
                                    throw (RuntimeException)jjte005;
                                }
                                if (jjte005 instanceof ParseException) {
                                    throw (ParseException)jjte005;
                                }
                                throw (Error)jjte005;
                            }
                        }
                        finally {
                            if (!jjtc005) continue block34;
                            this.jjtree.closeNodeScope((Node)jjtn005, 2);
                            this.jjtreeCloseNodeScope(jjtn005);
                            continue block34;
                        }
                    }
                }
                break;
            }
            this.jj_la1[6] = this.jj_gen;
            this.jj_consume_token(-1);
            throw new ParseException();
        }
    }

    public final void AdditiveExpression() throws ParseException {
        block26: {
            this.MultiplicativeExpression();
            block17: while (true) {
                switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                    case 40: 
                    case 41: {
                        break;
                    }
                    default: {
                        this.jj_la1[7] = this.jj_gen;
                        break block26;
                    }
                }
                switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                    case 40: {
                        this.jj_consume_token(40);
                        SimpleNode jjtn001 = new SimpleNode(10);
                        boolean jjtc001 = true;
                        this.jjtree.openNodeScope(jjtn001);
                        this.jjtreeOpenNodeScope(jjtn001);
                        try {
                            try {
                                this.MultiplicativeExpression();
                                continue block17;
                            }
                            catch (Throwable jjte001) {
                                if (jjtc001) {
                                    this.jjtree.clearNodeScope(jjtn001);
                                    jjtc001 = false;
                                } else {
                                    this.jjtree.popNode();
                                }
                                if (jjte001 instanceof RuntimeException) {
                                    throw (RuntimeException)jjte001;
                                }
                                if (jjte001 instanceof ParseException) {
                                    throw (ParseException)jjte001;
                                }
                                throw (Error)jjte001;
                            }
                        }
                        finally {
                            if (!jjtc001) continue block17;
                            this.jjtree.closeNodeScope((Node)jjtn001, 2);
                            this.jjtreeCloseNodeScope(jjtn001);
                            continue block17;
                        }
                    }
                    case 41: {
                        this.jj_consume_token(41);
                        SimpleNode jjtn002 = new SimpleNode(11);
                        boolean jjtc002 = true;
                        this.jjtree.openNodeScope(jjtn002);
                        this.jjtreeOpenNodeScope(jjtn002);
                        try {
                            try {
                                this.MultiplicativeExpression();
                                continue block17;
                            }
                            catch (Throwable jjte002) {
                                if (jjtc002) {
                                    this.jjtree.clearNodeScope(jjtn002);
                                    jjtc002 = false;
                                } else {
                                    this.jjtree.popNode();
                                }
                                if (jjte002 instanceof RuntimeException) {
                                    throw (RuntimeException)jjte002;
                                }
                                if (jjte002 instanceof ParseException) {
                                    throw (ParseException)jjte002;
                                }
                                throw (Error)jjte002;
                            }
                        }
                        finally {
                            if (!jjtc002) continue block17;
                            this.jjtree.closeNodeScope((Node)jjtn002, 2);
                            this.jjtreeCloseNodeScope(jjtn002);
                            continue block17;
                        }
                    }
                }
                break;
            }
            this.jj_la1[8] = this.jj_gen;
            this.jj_consume_token(-1);
            throw new ParseException();
        }
    }

    public final void MultiplicativeExpression() throws ParseException {
        block26: {
            this.UnaryExpression();
            block17: while (true) {
                switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                    case 42: 
                    case 43: {
                        break;
                    }
                    default: {
                        this.jj_la1[9] = this.jj_gen;
                        break block26;
                    }
                }
                switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                    case 42: {
                        this.jj_consume_token(42);
                        SimpleNode jjtn001 = new SimpleNode(12);
                        boolean jjtc001 = true;
                        this.jjtree.openNodeScope(jjtn001);
                        this.jjtreeOpenNodeScope(jjtn001);
                        try {
                            try {
                                this.UnaryExpression();
                                continue block17;
                            }
                            catch (Throwable jjte001) {
                                if (jjtc001) {
                                    this.jjtree.clearNodeScope(jjtn001);
                                    jjtc001 = false;
                                } else {
                                    this.jjtree.popNode();
                                }
                                if (jjte001 instanceof RuntimeException) {
                                    throw (RuntimeException)jjte001;
                                }
                                if (jjte001 instanceof ParseException) {
                                    throw (ParseException)jjte001;
                                }
                                throw (Error)jjte001;
                            }
                        }
                        finally {
                            if (!jjtc001) continue block17;
                            this.jjtree.closeNodeScope((Node)jjtn001, 2);
                            this.jjtreeCloseNodeScope(jjtn001);
                            continue block17;
                        }
                    }
                    case 43: {
                        this.jj_consume_token(43);
                        SimpleNode jjtn002 = new SimpleNode(13);
                        boolean jjtc002 = true;
                        this.jjtree.openNodeScope(jjtn002);
                        this.jjtreeOpenNodeScope(jjtn002);
                        try {
                            try {
                                this.UnaryExpression();
                                continue block17;
                            }
                            catch (Throwable jjte002) {
                                if (jjtc002) {
                                    this.jjtree.clearNodeScope(jjtn002);
                                    jjtc002 = false;
                                } else {
                                    this.jjtree.popNode();
                                }
                                if (jjte002 instanceof RuntimeException) {
                                    throw (RuntimeException)jjte002;
                                }
                                if (jjte002 instanceof ParseException) {
                                    throw (ParseException)jjte002;
                                }
                                throw (Error)jjte002;
                            }
                        }
                        finally {
                            if (!jjtc002) continue block17;
                            this.jjtree.closeNodeScope((Node)jjtn002, 2);
                            this.jjtreeCloseNodeScope(jjtn002);
                            continue block17;
                        }
                    }
                }
                break;
            }
            this.jj_la1[10] = this.jj_gen;
            this.jj_consume_token(-1);
            throw new ParseException();
        }
    }

    public final void UnaryExpression() throws ParseException {
        block2 : switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
            case 14: {
                this.jj_consume_token(14);
                SimpleNode jjtn001 = new SimpleNode(14);
                boolean jjtc001 = true;
                this.jjtree.openNodeScope(jjtn001);
                this.jjtreeOpenNodeScope(jjtn001);
                try {
                    try {
                        switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                            case 30: {
                                this.jj_consume_token(30);
                                this.Expression();
                                this.jj_consume_token(31);
                                break block2;
                            }
                            case 32: {
                                this.jj_consume_token(32);
                                this.Expression();
                                this.jj_consume_token(33);
                                break block2;
                            }
                            default: {
                                this.jj_la1[11] = this.jj_gen;
                                this.jj_consume_token(-1);
                                throw new ParseException();
                            }
                        }
                    }
                    catch (Throwable jjte001) {
                        if (jjtc001) {
                            this.jjtree.clearNodeScope(jjtn001);
                            jjtc001 = false;
                        } else {
                            this.jjtree.popNode();
                        }
                        if (jjte001 instanceof RuntimeException) {
                            throw (RuntimeException)jjte001;
                        }
                        if (jjte001 instanceof ParseException) {
                            throw (ParseException)jjte001;
                        }
                        throw (Error)jjte001;
                    }
                }
                finally {
                    if (jjtc001) {
                        this.jjtree.closeNodeScope((Node)jjtn001, 1);
                        this.jjtreeCloseNodeScope(jjtn001);
                    }
                }
            }
            case 8: 
            case 9: 
            case 21: 
            case 22: 
            case 23: 
            case 24: 
            case 25: 
            case 26: 
            case 27: 
            case 28: 
            case 29: 
            case 30: 
            case 32: 
            case 34: 
            case 35: 
            case 36: {
                this.PrimaryExpression();
                break;
            }
            default: {
                this.jj_la1[12] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
            }
        }
    }

    public final void PrimaryExpression() throws ParseException {
        if (this.jj_2_2(2)) {
            this.Literal();
        } else {
            switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                case 30: {
                    this.jj_consume_token(30);
                    this.Expression();
                    this.jj_consume_token(31);
                    break;
                }
                case 32: {
                    this.jj_consume_token(32);
                    this.Expression();
                    this.jj_consume_token(33);
                    break;
                }
                case 34: {
                    this.Evaluate();
                    break;
                }
                default: {
                    this.jj_la1[13] = this.jj_gen;
                    this.jj_consume_token(-1);
                    throw new ParseException();
                }
            }
        }
    }

    public final void Evaluate() throws ParseException {
        if (this.jj_2_3(2)) {
            this.Function();
        } else {
            switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                case 34: {
                    this.Attribute();
                    break;
                }
                default: {
                    this.jj_la1[14] = this.jj_gen;
                    this.jj_consume_token(-1);
                    throw new ParseException();
                }
            }
        }
    }

    public final void Attribute() throws ParseException {
        SimpleNode jjtn000 = new SimpleNode(15);
        boolean jjtc000 = true;
        this.jjtree.openNodeScope(jjtn000);
        this.jjtreeOpenNodeScope(jjtn000);
        try {
            this.jj_consume_token(34);
        }
        finally {
            if (jjtc000) {
                this.jjtree.closeNodeScope((Node)jjtn000, true);
                this.jjtreeCloseNodeScope(jjtn000);
            }
        }
    }

    public final void Literal() throws ParseException {
        switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
            case 35: {
                this.IntegerLiteral();
                break;
            }
            case 36: {
                this.FloatingLiteral();
                break;
            }
            case 21: 
            case 22: {
                this.BooleanLiteral();
                break;
            }
            case 8: 
            case 9: {
                this.StringLiteral();
                break;
            }
            case 23: 
            case 24: 
            case 25: 
            case 26: 
            case 27: 
            case 28: 
            case 29: {
                this.Geometry();
                break;
            }
            default: {
                this.jj_la1[15] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
            }
        }
    }

    public final void IntegerLiteral() throws ParseException {
        SimpleNode jjtn000 = new SimpleNode(16);
        boolean jjtc000 = true;
        this.jjtree.openNodeScope(jjtn000);
        this.jjtreeOpenNodeScope(jjtn000);
        try {
            this.jj_consume_token(35);
        }
        finally {
            if (jjtc000) {
                this.jjtree.closeNodeScope((Node)jjtn000, true);
                this.jjtreeCloseNodeScope(jjtn000);
            }
        }
    }

    public final void FloatingLiteral() throws ParseException {
        SimpleNode jjtn000 = new SimpleNode(17);
        boolean jjtc000 = true;
        this.jjtree.openNodeScope(jjtn000);
        this.jjtreeOpenNodeScope(jjtn000);
        try {
            this.jj_consume_token(36);
        }
        finally {
            if (jjtc000) {
                this.jjtree.closeNodeScope((Node)jjtn000, true);
                this.jjtreeCloseNodeScope(jjtn000);
            }
        }
    }

    public final void BooleanLiteral() throws ParseException {
        switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
            case 21: {
                SimpleNode jjtn001 = new SimpleNode(18);
                boolean jjtc001 = true;
                this.jjtree.openNodeScope(jjtn001);
                this.jjtreeOpenNodeScope(jjtn001);
                try {
                    this.jj_consume_token(21);
                    break;
                }
                finally {
                    if (jjtc001) {
                        this.jjtree.closeNodeScope((Node)jjtn001, true);
                        this.jjtreeCloseNodeScope(jjtn001);
                    }
                }
            }
            case 22: {
                SimpleNode jjtn002 = new SimpleNode(19);
                boolean jjtc002 = true;
                this.jjtree.openNodeScope(jjtn002);
                this.jjtreeOpenNodeScope(jjtn002);
                try {
                    this.jj_consume_token(22);
                    break;
                }
                finally {
                    if (jjtc002) {
                        this.jjtree.closeNodeScope((Node)jjtn002, true);
                        this.jjtreeCloseNodeScope(jjtn002);
                    }
                }
            }
            default: {
                this.jj_la1[16] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
            }
        }
    }

    public final void StringLiteral() throws ParseException {
        SimpleNode jjtn000 = new SimpleNode(20);
        boolean jjtc000 = true;
        this.jjtree.openNodeScope(jjtn000);
        this.jjtreeOpenNodeScope(jjtn000);
        try {
            switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                case 8: {
                    jjtn000.token = this.jj_consume_token(8);
                    break;
                }
                case 9: {
                    jjtn000.token = this.jj_consume_token(9);
                    break;
                }
                default: {
                    this.jj_la1[17] = this.jj_gen;
                    this.jj_consume_token(-1);
                    throw new ParseException();
                }
            }
        }
        finally {
            if (jjtc000) {
                this.jjtree.closeNodeScope((Node)jjtn000, true);
                this.jjtreeCloseNodeScope(jjtn000);
            }
        }
    }

    public final void Function() throws ParseException {
        SimpleNode jjtn000 = new SimpleNode(21);
        boolean jjtc000 = true;
        this.jjtree.openNodeScope(jjtn000);
        this.jjtreeOpenNodeScope(jjtn000);
        try {
            try {
                jjtn000.token = this.jj_consume_token(34);
                this.jj_consume_token(30);
                this.FunctionArg();
                block8: while (true) {
                    switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                        case 44: {
                            break;
                        }
                        default: {
                            this.jj_la1[18] = this.jj_gen;
                            break block8;
                        }
                    }
                    this.jj_consume_token(44);
                    this.FunctionArg();
                }
                this.jj_consume_token(31);
            }
            catch (Throwable jjte000) {
                if (jjtc000) {
                    this.jjtree.clearNodeScope(jjtn000);
                    jjtc000 = false;
                } else {
                    this.jjtree.popNode();
                }
                if (jjte000 instanceof RuntimeException) {
                    throw (RuntimeException)jjte000;
                }
                if (jjte000 instanceof ParseException) {
                    throw (ParseException)jjte000;
                }
                throw (Error)jjte000;
            }
        }
        finally {
            if (jjtc000) {
                this.jjtree.closeNodeScope((Node)jjtn000, true);
                this.jjtreeCloseNodeScope(jjtn000);
            }
        }
    }

    public final void FunctionArg() throws ParseException {
        switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
            case 8: 
            case 9: 
            case 21: 
            case 22: 
            case 23: 
            case 24: 
            case 25: 
            case 26: 
            case 27: 
            case 28: 
            case 29: 
            case 35: 
            case 36: {
                this.Literal();
                break;
            }
            case 34: {
                this.Evaluate();
                break;
            }
            case 31: {
                break;
            }
            default: {
                this.jj_la1[19] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
            }
        }
    }

    public final void Geometry() throws ParseException {
        switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
            case 23: {
                this.Point();
                break;
            }
            case 24: {
                this.LineString();
                break;
            }
            case 25: {
                this.Polygon();
                break;
            }
            case 26: {
                this.MultiPoint();
                break;
            }
            case 27: {
                this.MultiLineString();
                break;
            }
            case 28: {
                this.MultiPolygon();
                break;
            }
            case 29: {
                this.GeometryCollection();
                break;
            }
            default: {
                this.jj_la1[20] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
            }
        }
    }

    public final void Coord() throws ParseException {
        switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
            case 36: {
                this.jj_consume_token(36);
                break;
            }
            case 35: {
                this.jj_consume_token(35);
                break;
            }
            default: {
                this.jj_la1[21] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
            }
        }
        switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
            case 36: {
                this.jj_consume_token(36);
                break;
            }
            case 35: {
                this.jj_consume_token(35);
                break;
            }
            default: {
                this.jj_la1[22] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
            }
        }
        block8 : switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
            case 35: 
            case 36: {
                switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                    case 36: {
                        this.jj_consume_token(36);
                        break block8;
                    }
                    case 35: {
                        this.jj_consume_token(35);
                        break block8;
                    }
                }
                this.jj_la1[23] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
            }
            default: {
                this.jj_la1[24] = this.jj_gen;
            }
        }
    }

    public final void Coords() throws ParseException {
        this.jj_consume_token(30);
        this.Coord();
        block3: while (true) {
            switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                case 44: {
                    break;
                }
                default: {
                    this.jj_la1[25] = this.jj_gen;
                    break block3;
                }
            }
            this.jj_consume_token(44);
            this.Coord();
        }
        this.jj_consume_token(31);
    }

    public final void CoordsList() throws ParseException {
        this.jj_consume_token(30);
        this.Coords();
        block3: while (true) {
            switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                case 44: {
                    break;
                }
                default: {
                    this.jj_la1[26] = this.jj_gen;
                    break block3;
                }
            }
            this.jj_consume_token(44);
            this.Coords();
        }
        this.jj_consume_token(31);
    }

    public final void Point() throws ParseException {
        SimpleNode jjtn000 = new SimpleNode(22);
        boolean jjtc000 = true;
        this.jjtree.openNodeScope(jjtn000);
        this.jjtreeOpenNodeScope(jjtn000);
        try {
            try {
                jjtn000.token = this.jj_consume_token(23);
                this.jj_consume_token(30);
                this.Coord();
                this.jj_consume_token(31);
            }
            catch (Throwable jjte000) {
                if (jjtc000) {
                    this.jjtree.clearNodeScope(jjtn000);
                    jjtc000 = false;
                } else {
                    this.jjtree.popNode();
                }
                if (jjte000 instanceof RuntimeException) {
                    throw (RuntimeException)jjte000;
                }
                if (jjte000 instanceof ParseException) {
                    throw (ParseException)jjte000;
                }
                throw (Error)jjte000;
            }
        }
        finally {
            if (jjtc000) {
                this.jjtree.closeNodeScope((Node)jjtn000, true);
                this.jjtreeCloseNodeScope(jjtn000);
            }
        }
    }

    public final void LineString() throws ParseException {
        SimpleNode jjtn000 = new SimpleNode(22);
        boolean jjtc000 = true;
        this.jjtree.openNodeScope(jjtn000);
        this.jjtreeOpenNodeScope(jjtn000);
        try {
            try {
                jjtn000.token = this.jj_consume_token(24);
                this.Coords();
            }
            catch (Throwable jjte000) {
                if (jjtc000) {
                    this.jjtree.clearNodeScope(jjtn000);
                    jjtc000 = false;
                } else {
                    this.jjtree.popNode();
                }
                if (jjte000 instanceof RuntimeException) {
                    throw (RuntimeException)jjte000;
                }
                if (jjte000 instanceof ParseException) {
                    throw (ParseException)jjte000;
                }
                throw (Error)jjte000;
            }
        }
        finally {
            if (jjtc000) {
                this.jjtree.closeNodeScope((Node)jjtn000, true);
                this.jjtreeCloseNodeScope(jjtn000);
            }
        }
    }

    public final void Polygon() throws ParseException {
        SimpleNode jjtn000 = new SimpleNode(22);
        boolean jjtc000 = true;
        this.jjtree.openNodeScope(jjtn000);
        this.jjtreeOpenNodeScope(jjtn000);
        try {
            try {
                jjtn000.token = this.jj_consume_token(25);
                this.CoordsList();
            }
            catch (Throwable jjte000) {
                if (jjtc000) {
                    this.jjtree.clearNodeScope(jjtn000);
                    jjtc000 = false;
                } else {
                    this.jjtree.popNode();
                }
                if (jjte000 instanceof RuntimeException) {
                    throw (RuntimeException)jjte000;
                }
                if (jjte000 instanceof ParseException) {
                    throw (ParseException)jjte000;
                }
                throw (Error)jjte000;
            }
        }
        finally {
            if (jjtc000) {
                this.jjtree.closeNodeScope((Node)jjtn000, true);
                this.jjtreeCloseNodeScope(jjtn000);
            }
        }
    }

    public final void MultiPoint() throws ParseException {
        SimpleNode jjtn000 = new SimpleNode(22);
        boolean jjtc000 = true;
        this.jjtree.openNodeScope(jjtn000);
        this.jjtreeOpenNodeScope(jjtn000);
        try {
            try {
                jjtn000.token = this.jj_consume_token(26);
                this.CoordsList();
            }
            catch (Throwable jjte000) {
                if (jjtc000) {
                    this.jjtree.clearNodeScope(jjtn000);
                    jjtc000 = false;
                } else {
                    this.jjtree.popNode();
                }
                if (jjte000 instanceof RuntimeException) {
                    throw (RuntimeException)jjte000;
                }
                if (jjte000 instanceof ParseException) {
                    throw (ParseException)jjte000;
                }
                throw (Error)jjte000;
            }
        }
        finally {
            if (jjtc000) {
                this.jjtree.closeNodeScope((Node)jjtn000, true);
                this.jjtreeCloseNodeScope(jjtn000);
            }
        }
    }

    public final void MultiLineString() throws ParseException {
        SimpleNode jjtn000 = new SimpleNode(22);
        boolean jjtc000 = true;
        this.jjtree.openNodeScope(jjtn000);
        this.jjtreeOpenNodeScope(jjtn000);
        try {
            try {
                jjtn000.token = this.jj_consume_token(27);
                this.CoordsList();
            }
            catch (Throwable jjte000) {
                if (jjtc000) {
                    this.jjtree.clearNodeScope(jjtn000);
                    jjtc000 = false;
                } else {
                    this.jjtree.popNode();
                }
                if (jjte000 instanceof RuntimeException) {
                    throw (RuntimeException)jjte000;
                }
                if (jjte000 instanceof ParseException) {
                    throw (ParseException)jjte000;
                }
                throw (Error)jjte000;
            }
        }
        finally {
            if (jjtc000) {
                this.jjtree.closeNodeScope((Node)jjtn000, true);
                this.jjtreeCloseNodeScope(jjtn000);
            }
        }
    }

    public final void MultiPolygon() throws ParseException {
        SimpleNode jjtn000 = new SimpleNode(22);
        boolean jjtc000 = true;
        this.jjtree.openNodeScope(jjtn000);
        this.jjtreeOpenNodeScope(jjtn000);
        try {
            try {
                jjtn000.token = this.jj_consume_token(28);
                this.CoordsList();
            }
            catch (Throwable jjte000) {
                if (jjtc000) {
                    this.jjtree.clearNodeScope(jjtn000);
                    jjtc000 = false;
                } else {
                    this.jjtree.popNode();
                }
                if (jjte000 instanceof RuntimeException) {
                    throw (RuntimeException)jjte000;
                }
                if (jjte000 instanceof ParseException) {
                    throw (ParseException)jjte000;
                }
                throw (Error)jjte000;
            }
        }
        finally {
            if (jjtc000) {
                this.jjtree.closeNodeScope((Node)jjtn000, true);
                this.jjtreeCloseNodeScope(jjtn000);
            }
        }
    }

    public final void GeometryCollection() throws ParseException {
        SimpleNode jjtn000 = new SimpleNode(22);
        boolean jjtc000 = true;
        this.jjtree.openNodeScope(jjtn000);
        this.jjtreeOpenNodeScope(jjtn000);
        try {
            try {
                jjtn000.token = this.jj_consume_token(29);
                this.jj_consume_token(30);
                this.Geometry();
                block8: while (true) {
                    switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                        case 44: {
                            break;
                        }
                        default: {
                            this.jj_la1[27] = this.jj_gen;
                            break block8;
                        }
                    }
                    this.jj_consume_token(44);
                    this.Geometry();
                }
                this.jj_consume_token(31);
            }
            catch (Throwable jjte000) {
                if (jjtc000) {
                    this.jjtree.clearNodeScope(jjtn000);
                    jjtc000 = false;
                } else {
                    this.jjtree.popNode();
                }
                if (jjte000 instanceof RuntimeException) {
                    throw (RuntimeException)jjte000;
                }
                if (jjte000 instanceof ParseException) {
                    throw (ParseException)jjte000;
                }
                throw (Error)jjte000;
            }
        }
        finally {
            if (jjtc000) {
                this.jjtree.closeNodeScope((Node)jjtn000, true);
                this.jjtreeCloseNodeScope(jjtn000);
            }
        }
    }

    private final boolean jj_2_1(int xla) {
        this.jj_la = xla;
        this.jj_lastpos = this.jj_scanpos = this.token;
        boolean retval = !this.jj_3_1();
        this.jj_save(0, xla);
        return retval;
    }

    private final boolean jj_2_2(int xla) {
        this.jj_la = xla;
        this.jj_lastpos = this.jj_scanpos = this.token;
        boolean retval = !this.jj_3_2();
        this.jj_save(1, xla);
        return retval;
    }

    private final boolean jj_2_3(int xla) {
        this.jj_la = xla;
        this.jj_lastpos = this.jj_scanpos = this.token;
        boolean retval = !this.jj_3_3();
        this.jj_save(2, xla);
        return retval;
    }

    private final boolean jj_3R_67() {
        if (this.jj_3R_68()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_26() {
        if (this.jj_scan_token(35)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_53() {
        if (this.jj_scan_token(27)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        if (this.jj_3R_60()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_65() {
        if (this.jj_3R_67()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_21() {
        if (this.jj_3R_30()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_52() {
        if (this.jj_scan_token(26)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        if (this.jj_3R_60()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_20() {
        if (this.jj_3R_29()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_19() {
        if (this.jj_3R_28()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_18() {
        if (this.jj_3R_27()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_17() {
        if (this.jj_3R_26()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_13() {
        Token xsp = this.jj_scanpos;
        if (this.jj_3R_17()) {
            this.jj_scanpos = xsp;
            if (this.jj_3R_18()) {
                this.jj_scanpos = xsp;
                if (this.jj_3R_19()) {
                    this.jj_scanpos = xsp;
                    if (this.jj_3R_20()) {
                        this.jj_scanpos = xsp;
                        if (this.jj_3R_21()) {
                            return true;
                        }
                        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
                            return false;
                        }
                    } else if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
                        return false;
                    }
                } else if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
                    return false;
                }
            } else if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
                return false;
            }
        } else if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_47() {
        if (this.jj_scan_token(32)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_63() {
        if (this.jj_3R_65()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_51() {
        if (this.jj_scan_token(25)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        if (this.jj_3R_60()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_66() {
        if (this.jj_scan_token(34)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_61() {
        if (this.jj_3R_63()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_50() {
        if (this.jj_scan_token(24)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        if (this.jj_3R_59()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_64() {
        if (this.jj_3R_66()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_62() {
        Token xsp = this.jj_scanpos;
        if (this.jj_3_3()) {
            this.jj_scanpos = xsp;
            if (this.jj_3R_64()) {
                return true;
            }
            if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
                return false;
            }
        } else if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3_3() {
        if (this.jj_3R_14()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_49() {
        if (this.jj_scan_token(23)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        if (this.jj_scan_token(30)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_58() {
        if (this.jj_3R_62()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_60() {
        if (this.jj_scan_token(30)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_57() {
        if (this.jj_scan_token(32)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        if (this.jj_3R_61()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_56() {
        if (this.jj_scan_token(30)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        if (this.jj_3R_61()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_48() {
        Token xsp = this.jj_scanpos;
        if (this.jj_3_2()) {
            this.jj_scanpos = xsp;
            if (this.jj_3R_56()) {
                this.jj_scanpos = xsp;
                if (this.jj_3R_57()) {
                    this.jj_scanpos = xsp;
                    if (this.jj_3R_58()) {
                        return true;
                    }
                    if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
                        return false;
                    }
                } else if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
                    return false;
                }
            } else if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
                return false;
            }
        } else if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3_2() {
        if (this.jj_3R_13()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_46() {
        if (this.jj_scan_token(30)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_59() {
        if (this.jj_scan_token(30)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_32() {
        if (this.jj_3R_48()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_22() {
        Token xsp = this.jj_scanpos;
        if (this.jj_3R_31()) {
            this.jj_scanpos = xsp;
            if (this.jj_3R_32()) {
                return true;
            }
            if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
                return false;
            }
        } else if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_31() {
        if (this.jj_scan_token(14)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        Token xsp = this.jj_scanpos;
        if (this.jj_3R_46()) {
            this.jj_scanpos = xsp;
            if (this.jj_3R_47()) {
                return true;
            }
            if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
                return false;
            }
        } else if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_38() {
        if (this.jj_scan_token(9)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_34() {
        if (this.jj_scan_token(43)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_23() {
        Token xsp = this.jj_scanpos;
        if (this.jj_3R_33()) {
            this.jj_scanpos = xsp;
            if (this.jj_3R_34()) {
                return true;
            }
            if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
                return false;
            }
        } else if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_33() {
        if (this.jj_scan_token(42)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_45() {
        if (this.jj_3R_55()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_44() {
        if (this.jj_3R_54()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_43() {
        if (this.jj_3R_53()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_15() {
        Token xsp;
        block3: {
            if (this.jj_3R_22()) {
                return true;
            }
            if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
                return false;
            }
            do {
                xsp = this.jj_scanpos;
                if (this.jj_3R_23()) break block3;
            } while (this.jj_la != 0 || this.jj_scanpos != this.jj_lastpos);
            return false;
        }
        this.jj_scanpos = xsp;
        return false;
    }

    private final boolean jj_3R_42() {
        if (this.jj_3R_52()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_41() {
        if (this.jj_3R_51()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_40() {
        if (this.jj_3R_50()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_30() {
        Token xsp = this.jj_scanpos;
        if (this.jj_3R_39()) {
            this.jj_scanpos = xsp;
            if (this.jj_3R_40()) {
                this.jj_scanpos = xsp;
                if (this.jj_3R_41()) {
                    this.jj_scanpos = xsp;
                    if (this.jj_3R_42()) {
                        this.jj_scanpos = xsp;
                        if (this.jj_3R_43()) {
                            this.jj_scanpos = xsp;
                            if (this.jj_3R_44()) {
                                this.jj_scanpos = xsp;
                                if (this.jj_3R_45()) {
                                    return true;
                                }
                                if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
                                    return false;
                                }
                            } else if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
                                return false;
                            }
                        } else if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
                            return false;
                        }
                    } else if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
                        return false;
                    }
                } else if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
                    return false;
                }
            } else if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
                return false;
            }
        } else if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_39() {
        if (this.jj_3R_49()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_25() {
        if (this.jj_scan_token(41)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_24() {
        if (this.jj_scan_token(40)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_16() {
        Token xsp = this.jj_scanpos;
        if (this.jj_3R_24()) {
            this.jj_scanpos = xsp;
            if (this.jj_3R_25()) {
                return true;
            }
            if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
                return false;
            }
        } else if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_12() {
        Token xsp;
        block3: {
            if (this.jj_3R_15()) {
                return true;
            }
            if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
                return false;
            }
            do {
                xsp = this.jj_scanpos;
                if (this.jj_3R_16()) break block3;
            } while (this.jj_la != 0 || this.jj_scanpos != this.jj_lastpos);
            return false;
        }
        this.jj_scanpos = xsp;
        return false;
    }

    private final boolean jj_3R_14() {
        if (this.jj_scan_token(34)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        if (this.jj_scan_token(30)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_29() {
        Token xsp = this.jj_scanpos;
        if (this.jj_3R_37()) {
            this.jj_scanpos = xsp;
            if (this.jj_3R_38()) {
                return true;
            }
            if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
                return false;
            }
        } else if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_37() {
        if (this.jj_scan_token(8)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3_1() {
        if (this.jj_scan_token(18)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        if (this.jj_3R_12()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        if (this.jj_scan_token(18)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_36() {
        if (this.jj_scan_token(22)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_68() {
        if (this.jj_3R_12()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_28() {
        Token xsp = this.jj_scanpos;
        if (this.jj_3R_35()) {
            this.jj_scanpos = xsp;
            if (this.jj_3R_36()) {
                return true;
            }
            if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
                return false;
            }
        } else if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_35() {
        if (this.jj_scan_token(21)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_55() {
        if (this.jj_scan_token(29)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        if (this.jj_scan_token(30)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_27() {
        if (this.jj_scan_token(36)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_54() {
        if (this.jj_scan_token(28)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        if (this.jj_3R_60()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    public ExpressionParser(InputStream stream) {
        int[] nArray = new int[28];
        nArray[0] = 2145403648;
        nArray[1] = 8192;
        nArray[2] = 4096;
        nArray[3] = 98304;
        nArray[4] = 98304;
        nArray[5] = 0x1E0000;
        nArray[6] = 0x1E0000;
        nArray[11] = 0x40000000;
        nArray[12] = 2145403648;
        nArray[13] = 0x40000000;
        nArray[15] = 1071645440;
        nArray[16] = 0x600000;
        nArray[17] = 768;
        nArray[19] = 1071645440;
        nArray[20] = 1065353216;
        this.jj_la1_0 = nArray;
        int[] nArray2 = new int[28];
        nArray2[0] = 29;
        nArray2[7] = 768;
        nArray2[8] = 768;
        nArray2[9] = 3072;
        nArray2[10] = 3072;
        nArray2[11] = 1;
        nArray2[12] = 29;
        nArray2[13] = 5;
        nArray2[14] = 4;
        nArray2[15] = 24;
        nArray2[18] = 4096;
        nArray2[19] = 28;
        nArray2[21] = 24;
        nArray2[22] = 24;
        nArray2[23] = 24;
        nArray2[24] = 24;
        nArray2[25] = 4096;
        nArray2[26] = 4096;
        nArray2[27] = 4096;
        this.jj_la1_1 = nArray2;
        this.jj_2_rtns = new JJCalls[3];
        this.jj_rescan = false;
        this.jj_gc = 0;
        this.jj_expentries = new Vector();
        this.jj_kind = -1;
        this.jj_lasttokens = new int[100];
        this.jj_input_stream = new SimpleCharStream(stream, 1, 1);
        this.token_source = new ExpressionParserTokenManager(this.jj_input_stream);
        this.token = new Token();
        this.jj_ntk = -1;
        this.jj_gen = 0;
        int i = 0;
        while (i < 28) {
            this.jj_la1[i] = -1;
            ++i;
        }
        i = 0;
        while (i < this.jj_2_rtns.length) {
            this.jj_2_rtns[i] = new JJCalls();
            ++i;
        }
    }

    public void ReInit(InputStream stream) {
        this.jj_input_stream.ReInit(stream, 1, 1);
        this.token_source.ReInit(this.jj_input_stream);
        this.token = new Token();
        this.jj_ntk = -1;
        this.jjtree.reset();
        this.jj_gen = 0;
        int i = 0;
        while (i < 28) {
            this.jj_la1[i] = -1;
            ++i;
        }
        i = 0;
        while (i < this.jj_2_rtns.length) {
            this.jj_2_rtns[i] = new JJCalls();
            ++i;
        }
    }

    public ExpressionParser(Reader stream) {
        int[] nArray = new int[28];
        nArray[0] = 2145403648;
        nArray[1] = 8192;
        nArray[2] = 4096;
        nArray[3] = 98304;
        nArray[4] = 98304;
        nArray[5] = 0x1E0000;
        nArray[6] = 0x1E0000;
        nArray[11] = 0x40000000;
        nArray[12] = 2145403648;
        nArray[13] = 0x40000000;
        nArray[15] = 1071645440;
        nArray[16] = 0x600000;
        nArray[17] = 768;
        nArray[19] = 1071645440;
        nArray[20] = 1065353216;
        this.jj_la1_0 = nArray;
        int[] nArray2 = new int[28];
        nArray2[0] = 29;
        nArray2[7] = 768;
        nArray2[8] = 768;
        nArray2[9] = 3072;
        nArray2[10] = 3072;
        nArray2[11] = 1;
        nArray2[12] = 29;
        nArray2[13] = 5;
        nArray2[14] = 4;
        nArray2[15] = 24;
        nArray2[18] = 4096;
        nArray2[19] = 28;
        nArray2[21] = 24;
        nArray2[22] = 24;
        nArray2[23] = 24;
        nArray2[24] = 24;
        nArray2[25] = 4096;
        nArray2[26] = 4096;
        nArray2[27] = 4096;
        this.jj_la1_1 = nArray2;
        this.jj_2_rtns = new JJCalls[3];
        this.jj_rescan = false;
        this.jj_gc = 0;
        this.jj_expentries = new Vector();
        this.jj_kind = -1;
        this.jj_lasttokens = new int[100];
        this.jj_input_stream = new SimpleCharStream(stream, 1, 1);
        this.token_source = new ExpressionParserTokenManager(this.jj_input_stream);
        this.token = new Token();
        this.jj_ntk = -1;
        this.jj_gen = 0;
        int i = 0;
        while (i < 28) {
            this.jj_la1[i] = -1;
            ++i;
        }
        i = 0;
        while (i < this.jj_2_rtns.length) {
            this.jj_2_rtns[i] = new JJCalls();
            ++i;
        }
    }

    public void ReInit(Reader stream) {
        this.jj_input_stream.ReInit(stream, 1, 1);
        this.token_source.ReInit(this.jj_input_stream);
        this.token = new Token();
        this.jj_ntk = -1;
        this.jjtree.reset();
        this.jj_gen = 0;
        int i = 0;
        while (i < 28) {
            this.jj_la1[i] = -1;
            ++i;
        }
        i = 0;
        while (i < this.jj_2_rtns.length) {
            this.jj_2_rtns[i] = new JJCalls();
            ++i;
        }
    }

    public ExpressionParser(ExpressionParserTokenManager tm) {
        int[] nArray = new int[28];
        nArray[0] = 2145403648;
        nArray[1] = 8192;
        nArray[2] = 4096;
        nArray[3] = 98304;
        nArray[4] = 98304;
        nArray[5] = 0x1E0000;
        nArray[6] = 0x1E0000;
        nArray[11] = 0x40000000;
        nArray[12] = 2145403648;
        nArray[13] = 0x40000000;
        nArray[15] = 1071645440;
        nArray[16] = 0x600000;
        nArray[17] = 768;
        nArray[19] = 1071645440;
        nArray[20] = 1065353216;
        this.jj_la1_0 = nArray;
        int[] nArray2 = new int[28];
        nArray2[0] = 29;
        nArray2[7] = 768;
        nArray2[8] = 768;
        nArray2[9] = 3072;
        nArray2[10] = 3072;
        nArray2[11] = 1;
        nArray2[12] = 29;
        nArray2[13] = 5;
        nArray2[14] = 4;
        nArray2[15] = 24;
        nArray2[18] = 4096;
        nArray2[19] = 28;
        nArray2[21] = 24;
        nArray2[22] = 24;
        nArray2[23] = 24;
        nArray2[24] = 24;
        nArray2[25] = 4096;
        nArray2[26] = 4096;
        nArray2[27] = 4096;
        this.jj_la1_1 = nArray2;
        this.jj_2_rtns = new JJCalls[3];
        this.jj_rescan = false;
        this.jj_gc = 0;
        this.jj_expentries = new Vector();
        this.jj_kind = -1;
        this.jj_lasttokens = new int[100];
        this.token_source = tm;
        this.token = new Token();
        this.jj_ntk = -1;
        this.jj_gen = 0;
        int i = 0;
        while (i < 28) {
            this.jj_la1[i] = -1;
            ++i;
        }
        i = 0;
        while (i < this.jj_2_rtns.length) {
            this.jj_2_rtns[i] = new JJCalls();
            ++i;
        }
    }

    public void ReInit(ExpressionParserTokenManager tm) {
        this.token_source = tm;
        this.token = new Token();
        this.jj_ntk = -1;
        this.jjtree.reset();
        this.jj_gen = 0;
        int i = 0;
        while (i < 28) {
            this.jj_la1[i] = -1;
            ++i;
        }
        i = 0;
        while (i < this.jj_2_rtns.length) {
            this.jj_2_rtns[i] = new JJCalls();
            ++i;
        }
    }

    private final Token jj_consume_token(int kind) throws ParseException {
        Token oldToken = this.token;
        this.token = oldToken.next != null ? this.token.next : (this.token.next = this.token_source.getNextToken());
        this.jj_ntk = -1;
        if (this.token.kind == kind) {
            ++this.jj_gen;
            if (++this.jj_gc > 100) {
                this.jj_gc = 0;
                int i = 0;
                while (i < this.jj_2_rtns.length) {
                    JJCalls c = this.jj_2_rtns[i];
                    while (c != null) {
                        if (c.gen < this.jj_gen) {
                            c.first = null;
                        }
                        c = c.next;
                    }
                    ++i;
                }
            }
            return this.token;
        }
        this.token = oldToken;
        this.jj_kind = kind;
        throw this.generateParseException();
    }

    private final boolean jj_scan_token(int kind) {
        if (this.jj_scanpos == this.jj_lastpos) {
            --this.jj_la;
            if (this.jj_scanpos.next == null) {
                this.jj_scanpos = this.jj_scanpos.next = this.token_source.getNextToken();
                this.jj_lastpos = this.jj_scanpos.next;
            } else {
                this.jj_lastpos = this.jj_scanpos = this.jj_scanpos.next;
            }
        } else {
            this.jj_scanpos = this.jj_scanpos.next;
        }
        if (this.jj_rescan) {
            int i = 0;
            Token tok = this.token;
            while (tok != null && tok != this.jj_scanpos) {
                ++i;
                tok = tok.next;
            }
            if (tok != null) {
                this.jj_add_error_token(kind, i);
            }
        }
        return this.jj_scanpos.kind != kind;
    }

    public final Token getNextToken() {
        this.token = this.token.next != null ? this.token.next : (this.token.next = this.token_source.getNextToken());
        this.jj_ntk = -1;
        ++this.jj_gen;
        return this.token;
    }

    public final Token getToken(int index) {
        Token t = this.lookingAhead ? this.jj_scanpos : this.token;
        int i = 0;
        while (i < index) {
            t = t.next != null ? t.next : (t.next = this.token_source.getNextToken());
            ++i;
        }
        return t;
    }

    private final int jj_ntk() {
        this.jj_nt = this.token.next;
        if (this.jj_nt == null) {
            this.token.next = this.token_source.getNextToken();
            this.jj_ntk = this.token.next.kind;
            return this.jj_ntk;
        }
        this.jj_ntk = this.jj_nt.kind;
        return this.jj_ntk;
    }

    private void jj_add_error_token(int kind, int pos) {
        if (pos >= 100) {
            return;
        }
        if (pos == this.jj_endpos + 1) {
            this.jj_lasttokens[this.jj_endpos++] = kind;
        } else if (this.jj_endpos != 0) {
            this.jj_expentry = new int[this.jj_endpos];
            int i = 0;
            while (i < this.jj_endpos) {
                this.jj_expentry[i] = this.jj_lasttokens[i];
                ++i;
            }
            boolean exists = false;
            Enumeration enumeration = this.jj_expentries.elements();
            while (enumeration.hasMoreElements()) {
                int[] oldentry = (int[])enumeration.nextElement();
                if (oldentry.length != this.jj_expentry.length) continue;
                exists = true;
                int i2 = 0;
                while (i2 < this.jj_expentry.length) {
                    if (oldentry[i2] != this.jj_expentry[i2]) {
                        exists = false;
                        break;
                    }
                    ++i2;
                }
                if (exists) break;
            }
            if (!exists) {
                this.jj_expentries.addElement(this.jj_expentry);
            }
            if (pos != 0) {
                this.jj_endpos = pos;
                this.jj_lasttokens[this.jj_endpos - 1] = kind;
            }
        }
    }

    public final ParseException generateParseException() {
        this.jj_expentries.removeAllElements();
        boolean[] la1tokens = new boolean[45];
        int i = 0;
        while (i < 45) {
            la1tokens[i] = false;
            ++i;
        }
        if (this.jj_kind >= 0) {
            la1tokens[this.jj_kind] = true;
            this.jj_kind = -1;
        }
        i = 0;
        while (i < 28) {
            if (this.jj_la1[i] == this.jj_gen) {
                int j = 0;
                while (j < 32) {
                    if ((this.jj_la1_0[i] & 1 << j) != 0) {
                        la1tokens[j] = true;
                    }
                    if ((this.jj_la1_1[i] & 1 << j) != 0) {
                        la1tokens[32 + j] = true;
                    }
                    ++j;
                }
            }
            ++i;
        }
        i = 0;
        while (i < 45) {
            if (la1tokens[i]) {
                this.jj_expentry = new int[1];
                this.jj_expentry[0] = i;
                this.jj_expentries.addElement(this.jj_expentry);
            }
            ++i;
        }
        this.jj_endpos = 0;
        this.jj_rescan_token();
        this.jj_add_error_token(0, 0);
        int[][] exptokseq = new int[this.jj_expentries.size()][];
        int i2 = 0;
        while (i2 < this.jj_expentries.size()) {
            exptokseq[i2] = (int[])this.jj_expentries.elementAt(i2);
            ++i2;
        }
        return new ParseException(this.token, exptokseq, tokenImage);
    }

    public final void enable_tracing() {
    }

    public final void disable_tracing() {
    }

    private final void jj_rescan_token() {
        this.jj_rescan = true;
        int i = 0;
        while (i < 3) {
            JJCalls p = this.jj_2_rtns[i];
            do {
                if (p.gen <= this.jj_gen) continue;
                this.jj_la = p.arg;
                this.jj_lastpos = this.jj_scanpos = p.first;
                switch (i) {
                    case 0: {
                        this.jj_3_1();
                        break;
                    }
                    case 1: {
                        this.jj_3_2();
                        break;
                    }
                    case 2: {
                        this.jj_3_3();
                    }
                }
            } while ((p = p.next) != null);
            ++i;
        }
        this.jj_rescan = false;
    }

    private final void jj_save(int index, int xla) {
        JJCalls p = this.jj_2_rtns[index];
        while (p.gen > this.jj_gen) {
            if (p.next == null) {
                p = p.next = new JJCalls();
                break;
            }
            p = p.next;
        }
        p.gen = this.jj_gen + xla - this.jj_la;
        p.first = this.token;
        p.arg = xla;
    }

    static final class JJCalls {
        int gen;
        Token first;
        int arg;
        JJCalls next;

        JJCalls() {
        }
    }
}

