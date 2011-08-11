/* This file was generated by SableCC (http://www.sablecc.org/). */

package eu.serscis.sam.analysis;

import java.util.*;
import eu.serscis.sam.node.*;

public class AnalysisAdapter implements Analysis
{
    private Hashtable<Node,Object> in;
    private Hashtable<Node,Object> out;

    public Object getIn(Node node)
    {
        if(this.in == null)
        {
            return null;
        }

        return this.in.get(node);
    }

    public void setIn(Node node, Object o)
    {
        if(this.in == null)
        {
            this.in = new Hashtable<Node,Object>(1);
        }

        if(o != null)
        {
            this.in.put(node, o);
        }
        else
        {
            this.in.remove(node);
        }
    }

    public Object getOut(Node node)
    {
        if(this.out == null)
        {
            return null;
        }

        return this.out.get(node);
    }

    public void setOut(Node node, Object o)
    {
        if(this.out == null)
        {
            this.out = new Hashtable<Node,Object>(1);
        }

        if(o != null)
        {
            this.out.put(node, o);
        }
        else
        {
            this.out.remove(node);
        }
    }

    public void caseStart(Start node)
    {
        defaultCase(node);
    }

    public void caseAProgram(AProgram node)
    {
        defaultCase(node);
    }

    public void caseABehaviourToplevel(ABehaviourToplevel node)
    {
        defaultCase(node);
    }

    public void caseAConfigToplevel(AConfigToplevel node)
    {
        defaultCase(node);
    }

    public void caseAImportToplevel(AImportToplevel node)
    {
        defaultCase(node);
    }

    public void caseAFactToplevel(AFactToplevel node)
    {
        defaultCase(node);
    }

    public void caseARuleToplevel(ARuleToplevel node)
    {
        defaultCase(node);
    }

    public void caseADeclareToplevel(ADeclareToplevel node)
    {
        defaultCase(node);
    }

    public void caseAAssertToplevel(AAssertToplevel node)
    {
        defaultCase(node);
    }

    public void caseAQueryToplevel(AQueryToplevel node)
    {
        defaultCase(node);
    }

    public void caseAImport(AImport node)
    {
        defaultCase(node);
    }

    public void caseADeclare(ADeclare node)
    {
        defaultCase(node);
    }

    public void caseAFact(AFact node)
    {
        defaultCase(node);
    }

    public void caseARule(ARule node)
    {
        defaultCase(node);
    }

    public void caseALiterals(ALiterals node)
    {
        defaultCase(node);
    }

    public void caseALiteralTail(ALiteralTail node)
    {
        defaultCase(node);
    }

    public void caseAPositiveLiteral(APositiveLiteral node)
    {
        defaultCase(node);
    }

    public void caseANegativeLiteral(ANegativeLiteral node)
    {
        defaultCase(node);
    }

    public void caseANullaryAtom(ANullaryAtom node)
    {
        defaultCase(node);
    }

    public void caseANormalAtom(ANormalAtom node)
    {
        defaultCase(node);
    }

    public void caseABuiltinAtom(ABuiltinAtom node)
    {
        defaultCase(node);
    }

    public void caseATerms(ATerms node)
    {
        defaultCase(node);
    }

    public void caseATermTail(ATermTail node)
    {
        defaultCase(node);
    }

    public void caseAVarTerm(AVarTerm node)
    {
        defaultCase(node);
    }

    public void caseAStringTerm(AStringTerm node)
    {
        defaultCase(node);
    }

    public void caseAIntTerm(AIntTerm node)
    {
        defaultCase(node);
    }

    public void caseABoolTerm(ABoolTerm node)
    {
        defaultCase(node);
    }

    public void caseACompositeTerm(ACompositeTerm node)
    {
        defaultCase(node);
    }

    public void caseAQuery(AQuery node)
    {
        defaultCase(node);
    }

    public void caseAAssert(AAssert node)
    {
        defaultCase(node);
    }

    public void caseANamedPattern(ANamedPattern node)
    {
        defaultCase(node);
    }

    public void caseAAnyPattern(AAnyPattern node)
    {
        defaultCase(node);
    }

    public void caseAConfig(AConfig node)
    {
        defaultCase(node);
    }

    public void caseABehaviour(ABehaviour node)
    {
        defaultCase(node);
    }

    public void caseAExtends(AExtends node)
    {
        defaultCase(node);
    }

    public void caseAClassBody(AClassBody node)
    {
        defaultCase(node);
    }

    public void caseAConfigBody(AConfigBody node)
    {
        defaultCase(node);
    }

    public void caseAField(AField node)
    {
        defaultCase(node);
    }

    public void caseAConfigField(AConfigField node)
    {
        defaultCase(node);
    }

    public void caseAType(AType node)
    {
        defaultCase(node);
    }

    public void caseANoargsAnnotation(ANoargsAnnotation node)
    {
        defaultCase(node);
    }

    public void caseAArgsAnnotation(AArgsAnnotation node)
    {
        defaultCase(node);
    }

    public void caseAMethod(AMethod node)
    {
        defaultCase(node);
    }

    public void caseACode(ACode node)
    {
        defaultCase(node);
    }

    public void caseAAssign(AAssign node)
    {
        defaultCase(node);
    }

    public void caseAAssignStatement(AAssignStatement node)
    {
        defaultCase(node);
    }

    public void caseADeclStatement(ADeclStatement node)
    {
        defaultCase(node);
    }

    public void caseATryStatement(ATryStatement node)
    {
        defaultCase(node);
    }

    public void caseAThrowStatement(AThrowStatement node)
    {
        defaultCase(node);
    }

    public void caseAReturnStatement(AReturnStatement node)
    {
        defaultCase(node);
    }

    public void caseACatchBlock(ACatchBlock node)
    {
        defaultCase(node);
    }

    public void caseANewExpr(ANewExpr node)
    {
        defaultCase(node);
    }

    public void caseACallExpr(ACallExpr node)
    {
        defaultCase(node);
    }

    public void caseAStringExpr(AStringExpr node)
    {
        defaultCase(node);
    }

    public void caseACopyExpr(ACopyExpr node)
    {
        defaultCase(node);
    }

    public void caseAStringArgs(AStringArgs node)
    {
        defaultCase(node);
    }

    public void caseAStringArgsTail(AStringArgsTail node)
    {
        defaultCase(node);
    }

    public void caseAArgs(AArgs node)
    {
        defaultCase(node);
    }

    public void caseAArgsTail(AArgsTail node)
    {
        defaultCase(node);
    }

    public void caseAParam(AParam node)
    {
        defaultCase(node);
    }

    public void caseAParams(AParams node)
    {
        defaultCase(node);
    }

    public void caseAParamsTail(AParamsTail node)
    {
        defaultCase(node);
    }

    public void caseANamedblock(ANamedblock node)
    {
        defaultCase(node);
    }

    public void caseTNumber(TNumber node)
    {
        defaultCase(node);
    }

    public void caseTLPar(TLPar node)
    {
        defaultCase(node);
    }

    public void caseTRPar(TRPar node)
    {
        defaultCase(node);
    }

    public void caseTPublicTok(TPublicTok node)
    {
        defaultCase(node);
    }

    public void caseTPrivateTok(TPrivateTok node)
    {
        defaultCase(node);
    }

    public void caseTExtendsTok(TExtendsTok node)
    {
        defaultCase(node);
    }

    public void caseTNew(TNew node)
    {
        defaultCase(node);
    }

    public void caseTThrow(TThrow node)
    {
        defaultCase(node);
    }

    public void caseTReturn(TReturn node)
    {
        defaultCase(node);
    }

    public void caseTClassTok(TClassTok node)
    {
        defaultCase(node);
    }

    public void caseTAtTok(TAtTok node)
    {
        defaultCase(node);
    }

    public void caseTLBrace(TLBrace node)
    {
        defaultCase(node);
    }

    public void caseTRBrace(TRBrace node)
    {
        defaultCase(node);
    }

    public void caseTEq(TEq node)
    {
        defaultCase(node);
    }

    public void caseTBlank(TBlank node)
    {
        defaultCase(node);
    }

    public void caseTComment(TComment node)
    {
        defaultCase(node);
    }

    public void caseTBlockComment(TBlockComment node)
    {
        defaultCase(node);
    }

    public void caseTComma(TComma node)
    {
        defaultCase(node);
    }

    public void caseTGoal(TGoal node)
    {
        defaultCase(node);
    }

    public void caseTSemi(TSemi node)
    {
        defaultCase(node);
    }

    public void caseTIfDl(TIfDl node)
    {
        defaultCase(node);
    }

    public void caseTBang(TBang node)
    {
        defaultCase(node);
    }

    public void caseTStringLiteral(TStringLiteral node)
    {
        defaultCase(node);
    }

    public void caseTQueryStart(TQueryStart node)
    {
        defaultCase(node);
    }

    public void caseTQuestion(TQuestion node)
    {
        defaultCase(node);
    }

    public void caseTBinop(TBinop node)
    {
        defaultCase(node);
    }

    public void caseTStar(TStar node)
    {
        defaultCase(node);
    }

    public void caseTDots(TDots node)
    {
        defaultCase(node);
    }

    public void caseTDot(TDot node)
    {
        defaultCase(node);
    }

    public void caseTDeclareTok(TDeclareTok node)
    {
        defaultCase(node);
    }

    public void caseTImportTok(TImportTok node)
    {
        defaultCase(node);
    }

    public void caseTConfigTok(TConfigTok node)
    {
        defaultCase(node);
    }

    public void caseTTry(TTry node)
    {
        defaultCase(node);
    }

    public void caseTCatch(TCatch node)
    {
        defaultCase(node);
    }

    public void caseTAssertTok(TAssertTok node)
    {
        defaultCase(node);
    }

    public void caseTBool(TBool node)
    {
        defaultCase(node);
    }

    public void caseTName(TName node)
    {
        defaultCase(node);
    }

    public void caseEOF(EOF node)
    {
        defaultCase(node);
    }

    public void defaultCase(@SuppressWarnings("unused") Node node)
    {
        // do nothing
    }
}
