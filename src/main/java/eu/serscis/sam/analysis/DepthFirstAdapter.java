/* This file was generated by SableCC (http://www.sablecc.org/). */

package eu.serscis.sam.analysis;

import java.util.*;
import eu.serscis.sam.node.*;

public class DepthFirstAdapter extends AnalysisAdapter
{
    public void inStart(Start node)
    {
        defaultIn(node);
    }

    public void outStart(Start node)
    {
        defaultOut(node);
    }

    public void defaultIn(@SuppressWarnings("unused") Node node)
    {
        // Do nothing
    }

    public void defaultOut(@SuppressWarnings("unused") Node node)
    {
        // Do nothing
    }

    @Override
    public void caseStart(Start node)
    {
        inStart(node);
        node.getPProgram().apply(this);
        node.getEOF().apply(this);
        outStart(node);
    }

    public void inAProgram(AProgram node)
    {
        defaultIn(node);
    }

    public void outAProgram(AProgram node)
    {
        defaultOut(node);
    }

    @Override
    public void caseAProgram(AProgram node)
    {
        inAProgram(node);
        {
            List<PToplevel> copy = new ArrayList<PToplevel>(node.getToplevel());
            for(PToplevel e : copy)
            {
                e.apply(this);
            }
        }
        outAProgram(node);
    }

    public void inABehaviourToplevel(ABehaviourToplevel node)
    {
        defaultIn(node);
    }

    public void outABehaviourToplevel(ABehaviourToplevel node)
    {
        defaultOut(node);
    }

    @Override
    public void caseABehaviourToplevel(ABehaviourToplevel node)
    {
        inABehaviourToplevel(node);
        if(node.getBehaviour() != null)
        {
            node.getBehaviour().apply(this);
        }
        outABehaviourToplevel(node);
    }

    public void inAConfigToplevel(AConfigToplevel node)
    {
        defaultIn(node);
    }

    public void outAConfigToplevel(AConfigToplevel node)
    {
        defaultOut(node);
    }

    @Override
    public void caseAConfigToplevel(AConfigToplevel node)
    {
        inAConfigToplevel(node);
        if(node.getConfig() != null)
        {
            node.getConfig().apply(this);
        }
        outAConfigToplevel(node);
    }

    public void inAImportToplevel(AImportToplevel node)
    {
        defaultIn(node);
    }

    public void outAImportToplevel(AImportToplevel node)
    {
        defaultOut(node);
    }

    @Override
    public void caseAImportToplevel(AImportToplevel node)
    {
        inAImportToplevel(node);
        if(node.getImport() != null)
        {
            node.getImport().apply(this);
        }
        outAImportToplevel(node);
    }

    public void inAFactToplevel(AFactToplevel node)
    {
        defaultIn(node);
    }

    public void outAFactToplevel(AFactToplevel node)
    {
        defaultOut(node);
    }

    @Override
    public void caseAFactToplevel(AFactToplevel node)
    {
        inAFactToplevel(node);
        if(node.getFact() != null)
        {
            node.getFact().apply(this);
        }
        outAFactToplevel(node);
    }

    public void inARuleToplevel(ARuleToplevel node)
    {
        defaultIn(node);
    }

    public void outARuleToplevel(ARuleToplevel node)
    {
        defaultOut(node);
    }

    @Override
    public void caseARuleToplevel(ARuleToplevel node)
    {
        inARuleToplevel(node);
        if(node.getRule() != null)
        {
            node.getRule().apply(this);
        }
        outARuleToplevel(node);
    }

    public void inADeclareToplevel(ADeclareToplevel node)
    {
        defaultIn(node);
    }

    public void outADeclareToplevel(ADeclareToplevel node)
    {
        defaultOut(node);
    }

    @Override
    public void caseADeclareToplevel(ADeclareToplevel node)
    {
        inADeclareToplevel(node);
        if(node.getDeclare() != null)
        {
            node.getDeclare().apply(this);
        }
        outADeclareToplevel(node);
    }

    public void inAQueryToplevel(AQueryToplevel node)
    {
        defaultIn(node);
    }

    public void outAQueryToplevel(AQueryToplevel node)
    {
        defaultOut(node);
    }

    @Override
    public void caseAQueryToplevel(AQueryToplevel node)
    {
        inAQueryToplevel(node);
        if(node.getQuery() != null)
        {
            node.getQuery().apply(this);
        }
        outAQueryToplevel(node);
    }

    public void inAImport(AImport node)
    {
        defaultIn(node);
    }

    public void outAImport(AImport node)
    {
        defaultOut(node);
    }

    @Override
    public void caseAImport(AImport node)
    {
        inAImport(node);
        if(node.getImportTok() != null)
        {
            node.getImportTok().apply(this);
        }
        if(node.getStringLiteral() != null)
        {
            node.getStringLiteral().apply(this);
        }
        if(node.getDot() != null)
        {
            node.getDot().apply(this);
        }
        outAImport(node);
    }

    public void inADeclare(ADeclare node)
    {
        defaultIn(node);
    }

    public void outADeclare(ADeclare node)
    {
        defaultOut(node);
    }

    @Override
    public void caseADeclare(ADeclare node)
    {
        inADeclare(node);
        if(node.getDeclareTok() != null)
        {
            node.getDeclareTok().apply(this);
        }
        if(node.getAtom() != null)
        {
            node.getAtom().apply(this);
        }
        if(node.getDot() != null)
        {
            node.getDot().apply(this);
        }
        outADeclare(node);
    }

    public void inAFact(AFact node)
    {
        defaultIn(node);
    }

    public void outAFact(AFact node)
    {
        defaultOut(node);
    }

    @Override
    public void caseAFact(AFact node)
    {
        inAFact(node);
        if(node.getAtom() != null)
        {
            node.getAtom().apply(this);
        }
        if(node.getDot() != null)
        {
            node.getDot().apply(this);
        }
        outAFact(node);
    }

    public void inARule(ARule node)
    {
        defaultIn(node);
    }

    public void outARule(ARule node)
    {
        defaultOut(node);
    }

    @Override
    public void caseARule(ARule node)
    {
        inARule(node);
        if(node.getHead() != null)
        {
            node.getHead().apply(this);
        }
        if(node.getIfDl() != null)
        {
            node.getIfDl().apply(this);
        }
        if(node.getBody() != null)
        {
            node.getBody().apply(this);
        }
        if(node.getDot() != null)
        {
            node.getDot().apply(this);
        }
        outARule(node);
    }

    public void inALiterals(ALiterals node)
    {
        defaultIn(node);
    }

    public void outALiterals(ALiterals node)
    {
        defaultOut(node);
    }

    @Override
    public void caseALiterals(ALiterals node)
    {
        inALiterals(node);
        if(node.getLiteral() != null)
        {
            node.getLiteral().apply(this);
        }
        {
            List<PLiteralTail> copy = new ArrayList<PLiteralTail>(node.getLiteralTail());
            for(PLiteralTail e : copy)
            {
                e.apply(this);
            }
        }
        outALiterals(node);
    }

    public void inALiteralTail(ALiteralTail node)
    {
        defaultIn(node);
    }

    public void outALiteralTail(ALiteralTail node)
    {
        defaultOut(node);
    }

    @Override
    public void caseALiteralTail(ALiteralTail node)
    {
        inALiteralTail(node);
        if(node.getComma() != null)
        {
            node.getComma().apply(this);
        }
        if(node.getLiteral() != null)
        {
            node.getLiteral().apply(this);
        }
        outALiteralTail(node);
    }

    public void inAPositiveLiteral(APositiveLiteral node)
    {
        defaultIn(node);
    }

    public void outAPositiveLiteral(APositiveLiteral node)
    {
        defaultOut(node);
    }

    @Override
    public void caseAPositiveLiteral(APositiveLiteral node)
    {
        inAPositiveLiteral(node);
        if(node.getAtom() != null)
        {
            node.getAtom().apply(this);
        }
        outAPositiveLiteral(node);
    }

    public void inANegativeLiteral(ANegativeLiteral node)
    {
        defaultIn(node);
    }

    public void outANegativeLiteral(ANegativeLiteral node)
    {
        defaultOut(node);
    }

    @Override
    public void caseANegativeLiteral(ANegativeLiteral node)
    {
        inANegativeLiteral(node);
        if(node.getBang() != null)
        {
            node.getBang().apply(this);
        }
        if(node.getAtom() != null)
        {
            node.getAtom().apply(this);
        }
        outANegativeLiteral(node);
    }

    public void inANullaryAtom(ANullaryAtom node)
    {
        defaultIn(node);
    }

    public void outANullaryAtom(ANullaryAtom node)
    {
        defaultOut(node);
    }

    @Override
    public void caseANullaryAtom(ANullaryAtom node)
    {
        inANullaryAtom(node);
        if(node.getName() != null)
        {
            node.getName().apply(this);
        }
        outANullaryAtom(node);
    }

    public void inANormalAtom(ANormalAtom node)
    {
        defaultIn(node);
    }

    public void outANormalAtom(ANormalAtom node)
    {
        defaultOut(node);
    }

    @Override
    public void caseANormalAtom(ANormalAtom node)
    {
        inANormalAtom(node);
        if(node.getName() != null)
        {
            node.getName().apply(this);
        }
        if(node.getLPar() != null)
        {
            node.getLPar().apply(this);
        }
        if(node.getTerms() != null)
        {
            node.getTerms().apply(this);
        }
        if(node.getRPar() != null)
        {
            node.getRPar().apply(this);
        }
        outANormalAtom(node);
    }

    public void inABuiltinAtom(ABuiltinAtom node)
    {
        defaultIn(node);
    }

    public void outABuiltinAtom(ABuiltinAtom node)
    {
        defaultOut(node);
    }

    @Override
    public void caseABuiltinAtom(ABuiltinAtom node)
    {
        inABuiltinAtom(node);
        if(node.getLhs() != null)
        {
            node.getLhs().apply(this);
        }
        if(node.getBinop() != null)
        {
            node.getBinop().apply(this);
        }
        if(node.getRhs() != null)
        {
            node.getRhs().apply(this);
        }
        outABuiltinAtom(node);
    }

    public void inATerms(ATerms node)
    {
        defaultIn(node);
    }

    public void outATerms(ATerms node)
    {
        defaultOut(node);
    }

    @Override
    public void caseATerms(ATerms node)
    {
        inATerms(node);
        if(node.getTerm() != null)
        {
            node.getTerm().apply(this);
        }
        {
            List<PTermTail> copy = new ArrayList<PTermTail>(node.getTermTail());
            for(PTermTail e : copy)
            {
                e.apply(this);
            }
        }
        outATerms(node);
    }

    public void inATermTail(ATermTail node)
    {
        defaultIn(node);
    }

    public void outATermTail(ATermTail node)
    {
        defaultOut(node);
    }

    @Override
    public void caseATermTail(ATermTail node)
    {
        inATermTail(node);
        if(node.getComma() != null)
        {
            node.getComma().apply(this);
        }
        if(node.getTerm() != null)
        {
            node.getTerm().apply(this);
        }
        outATermTail(node);
    }

    public void inAVarTerm(AVarTerm node)
    {
        defaultIn(node);
    }

    public void outAVarTerm(AVarTerm node)
    {
        defaultOut(node);
    }

    @Override
    public void caseAVarTerm(AVarTerm node)
    {
        inAVarTerm(node);
        if(node.getQuestion() != null)
        {
            node.getQuestion().apply(this);
        }
        if(node.getName() != null)
        {
            node.getName().apply(this);
        }
        outAVarTerm(node);
    }

    public void inAStringTerm(AStringTerm node)
    {
        defaultIn(node);
    }

    public void outAStringTerm(AStringTerm node)
    {
        defaultOut(node);
    }

    @Override
    public void caseAStringTerm(AStringTerm node)
    {
        inAStringTerm(node);
        if(node.getStringLiteral() != null)
        {
            node.getStringLiteral().apply(this);
        }
        outAStringTerm(node);
    }

    public void inAIntTerm(AIntTerm node)
    {
        defaultIn(node);
    }

    public void outAIntTerm(AIntTerm node)
    {
        defaultOut(node);
    }

    @Override
    public void caseAIntTerm(AIntTerm node)
    {
        inAIntTerm(node);
        if(node.getNumber() != null)
        {
            node.getNumber().apply(this);
        }
        outAIntTerm(node);
    }

    public void inACompositeTerm(ACompositeTerm node)
    {
        defaultIn(node);
    }

    public void outACompositeTerm(ACompositeTerm node)
    {
        defaultOut(node);
    }

    @Override
    public void caseACompositeTerm(ACompositeTerm node)
    {
        inACompositeTerm(node);
        if(node.getName() != null)
        {
            node.getName().apply(this);
        }
        if(node.getLPar() != null)
        {
            node.getLPar().apply(this);
        }
        if(node.getTerms() != null)
        {
            node.getTerms().apply(this);
        }
        if(node.getRPar() != null)
        {
            node.getRPar().apply(this);
        }
        outACompositeTerm(node);
    }

    public void inAQuery(AQuery node)
    {
        defaultIn(node);
    }

    public void outAQuery(AQuery node)
    {
        defaultOut(node);
    }

    @Override
    public void caseAQuery(AQuery node)
    {
        inAQuery(node);
        if(node.getQueryStart() != null)
        {
            node.getQueryStart().apply(this);
        }
        if(node.getLiterals() != null)
        {
            node.getLiterals().apply(this);
        }
        if(node.getDot() != null)
        {
            node.getDot().apply(this);
        }
        outAQuery(node);
    }

    public void inANamedPattern(ANamedPattern node)
    {
        defaultIn(node);
    }

    public void outANamedPattern(ANamedPattern node)
    {
        defaultOut(node);
    }

    @Override
    public void caseANamedPattern(ANamedPattern node)
    {
        inANamedPattern(node);
        if(node.getName() != null)
        {
            node.getName().apply(this);
        }
        outANamedPattern(node);
    }

    public void inAAnyPattern(AAnyPattern node)
    {
        defaultIn(node);
    }

    public void outAAnyPattern(AAnyPattern node)
    {
        defaultOut(node);
    }

    @Override
    public void caseAAnyPattern(AAnyPattern node)
    {
        inAAnyPattern(node);
        if(node.getStar() != null)
        {
            node.getStar().apply(this);
        }
        outAAnyPattern(node);
    }

    public void inAConfig(AConfig node)
    {
        defaultIn(node);
    }

    public void outAConfig(AConfig node)
    {
        defaultOut(node);
    }

    @Override
    public void caseAConfig(AConfig node)
    {
        inAConfig(node);
        if(node.getConfigTok() != null)
        {
            node.getConfigTok().apply(this);
        }
        if(node.getLBrace() != null)
        {
            node.getLBrace().apply(this);
        }
        if(node.getConfigBody() != null)
        {
            node.getConfigBody().apply(this);
        }
        if(node.getRBrace() != null)
        {
            node.getRBrace().apply(this);
        }
        outAConfig(node);
    }

    public void inABehaviour(ABehaviour node)
    {
        defaultIn(node);
    }

    public void outABehaviour(ABehaviour node)
    {
        defaultOut(node);
    }

    @Override
    public void caseABehaviour(ABehaviour node)
    {
        inABehaviour(node);
        if(node.getClassTok() != null)
        {
            node.getClassTok().apply(this);
        }
        if(node.getName() != null)
        {
            node.getName().apply(this);
        }
        if(node.getExtends() != null)
        {
            node.getExtends().apply(this);
        }
        if(node.getLBrace() != null)
        {
            node.getLBrace().apply(this);
        }
        if(node.getClassBody() != null)
        {
            node.getClassBody().apply(this);
        }
        if(node.getRBrace() != null)
        {
            node.getRBrace().apply(this);
        }
        outABehaviour(node);
    }

    public void inAExtends(AExtends node)
    {
        defaultIn(node);
    }

    public void outAExtends(AExtends node)
    {
        defaultOut(node);
    }

    @Override
    public void caseAExtends(AExtends node)
    {
        inAExtends(node);
        if(node.getExtendsTok() != null)
        {
            node.getExtendsTok().apply(this);
        }
        if(node.getName() != null)
        {
            node.getName().apply(this);
        }
        outAExtends(node);
    }

    public void inAClassBody(AClassBody node)
    {
        defaultIn(node);
    }

    public void outAClassBody(AClassBody node)
    {
        defaultOut(node);
    }

    @Override
    public void caseAClassBody(AClassBody node)
    {
        inAClassBody(node);
        {
            List<PField> copy = new ArrayList<PField>(node.getField());
            for(PField e : copy)
            {
                e.apply(this);
            }
        }
        {
            List<PMethod> copy = new ArrayList<PMethod>(node.getMethod());
            for(PMethod e : copy)
            {
                e.apply(this);
            }
        }
        outAClassBody(node);
    }

    public void inAConfigBody(AConfigBody node)
    {
        defaultIn(node);
    }

    public void outAConfigBody(AConfigBody node)
    {
        defaultOut(node);
    }

    @Override
    public void caseAConfigBody(AConfigBody node)
    {
        inAConfigBody(node);
        {
            List<PConfigField> copy = new ArrayList<PConfigField>(node.getConfigField());
            for(PConfigField e : copy)
            {
                e.apply(this);
            }
        }
        {
            List<PNamedblock> copy = new ArrayList<PNamedblock>(node.getNamedblock());
            for(PNamedblock e : copy)
            {
                e.apply(this);
            }
        }
        outAConfigBody(node);
    }

    public void inAField(AField node)
    {
        defaultIn(node);
    }

    public void outAField(AField node)
    {
        defaultOut(node);
    }

    @Override
    public void caseAField(AField node)
    {
        inAField(node);
        if(node.getPrivateTok() != null)
        {
            node.getPrivateTok().apply(this);
        }
        if(node.getType() != null)
        {
            node.getType().apply(this);
        }
        if(node.getName() != null)
        {
            node.getName().apply(this);
        }
        if(node.getSemi() != null)
        {
            node.getSemi().apply(this);
        }
        outAField(node);
    }

    public void inAConfigField(AConfigField node)
    {
        defaultIn(node);
    }

    public void outAConfigField(AConfigField node)
    {
        defaultOut(node);
    }

    @Override
    public void caseAConfigField(AConfigField node)
    {
        inAConfigField(node);
        if(node.getType() != null)
        {
            node.getType().apply(this);
        }
        if(node.getName() != null)
        {
            node.getName().apply(this);
        }
        if(node.getSemi() != null)
        {
            node.getSemi().apply(this);
        }
        outAConfigField(node);
    }

    public void inAType(AType node)
    {
        defaultIn(node);
    }

    public void outAType(AType node)
    {
        defaultOut(node);
    }

    @Override
    public void caseAType(AType node)
    {
        inAType(node);
        if(node.getName() != null)
        {
            node.getName().apply(this);
        }
        outAType(node);
    }

    public void inANoargsAnnotation(ANoargsAnnotation node)
    {
        defaultIn(node);
    }

    public void outANoargsAnnotation(ANoargsAnnotation node)
    {
        defaultOut(node);
    }

    @Override
    public void caseANoargsAnnotation(ANoargsAnnotation node)
    {
        inANoargsAnnotation(node);
        if(node.getAtTok() != null)
        {
            node.getAtTok().apply(this);
        }
        if(node.getName() != null)
        {
            node.getName().apply(this);
        }
        outANoargsAnnotation(node);
    }

    public void inAArgsAnnotation(AArgsAnnotation node)
    {
        defaultIn(node);
    }

    public void outAArgsAnnotation(AArgsAnnotation node)
    {
        defaultOut(node);
    }

    @Override
    public void caseAArgsAnnotation(AArgsAnnotation node)
    {
        inAArgsAnnotation(node);
        if(node.getAtTok() != null)
        {
            node.getAtTok().apply(this);
        }
        if(node.getName() != null)
        {
            node.getName().apply(this);
        }
        if(node.getLPar() != null)
        {
            node.getLPar().apply(this);
        }
        if(node.getStringArgs() != null)
        {
            node.getStringArgs().apply(this);
        }
        if(node.getRPar() != null)
        {
            node.getRPar().apply(this);
        }
        outAArgsAnnotation(node);
    }

    public void inAMethod(AMethod node)
    {
        defaultIn(node);
    }

    public void outAMethod(AMethod node)
    {
        defaultOut(node);
    }

    @Override
    public void caseAMethod(AMethod node)
    {
        inAMethod(node);
        {
            List<PAnnotation> copy = new ArrayList<PAnnotation>(node.getAnnotation());
            for(PAnnotation e : copy)
            {
                e.apply(this);
            }
        }
        if(node.getPublicTok() != null)
        {
            node.getPublicTok().apply(this);
        }
        if(node.getType() != null)
        {
            node.getType().apply(this);
        }
        if(node.getName() != null)
        {
            node.getName().apply(this);
        }
        if(node.getLPar() != null)
        {
            node.getLPar().apply(this);
        }
        if(node.getParams() != null)
        {
            node.getParams().apply(this);
        }
        if(node.getStar() != null)
        {
            node.getStar().apply(this);
        }
        if(node.getRPar() != null)
        {
            node.getRPar().apply(this);
        }
        if(node.getLBrace() != null)
        {
            node.getLBrace().apply(this);
        }
        if(node.getCode() != null)
        {
            node.getCode().apply(this);
        }
        if(node.getRBrace() != null)
        {
            node.getRBrace().apply(this);
        }
        outAMethod(node);
    }

    public void inACode(ACode node)
    {
        defaultIn(node);
    }

    public void outACode(ACode node)
    {
        defaultOut(node);
    }

    @Override
    public void caseACode(ACode node)
    {
        inACode(node);
        {
            List<PStatement> copy = new ArrayList<PStatement>(node.getStatement());
            for(PStatement e : copy)
            {
                e.apply(this);
            }
        }
        outACode(node);
    }

    public void inAAssign(AAssign node)
    {
        defaultIn(node);
    }

    public void outAAssign(AAssign node)
    {
        defaultOut(node);
    }

    @Override
    public void caseAAssign(AAssign node)
    {
        inAAssign(node);
        if(node.getType() != null)
        {
            node.getType().apply(this);
        }
        if(node.getName() != null)
        {
            node.getName().apply(this);
        }
        if(node.getEq() != null)
        {
            node.getEq().apply(this);
        }
        outAAssign(node);
    }

    public void inAAssignStatement(AAssignStatement node)
    {
        defaultIn(node);
    }

    public void outAAssignStatement(AAssignStatement node)
    {
        defaultOut(node);
    }

    @Override
    public void caseAAssignStatement(AAssignStatement node)
    {
        inAAssignStatement(node);
        if(node.getAssign() != null)
        {
            node.getAssign().apply(this);
        }
        if(node.getExpr() != null)
        {
            node.getExpr().apply(this);
        }
        if(node.getSemi() != null)
        {
            node.getSemi().apply(this);
        }
        outAAssignStatement(node);
    }

    public void inADeclStatement(ADeclStatement node)
    {
        defaultIn(node);
    }

    public void outADeclStatement(ADeclStatement node)
    {
        defaultOut(node);
    }

    @Override
    public void caseADeclStatement(ADeclStatement node)
    {
        inADeclStatement(node);
        if(node.getType() != null)
        {
            node.getType().apply(this);
        }
        if(node.getName() != null)
        {
            node.getName().apply(this);
        }
        if(node.getSemi() != null)
        {
            node.getSemi().apply(this);
        }
        outADeclStatement(node);
    }

    public void inATryStatement(ATryStatement node)
    {
        defaultIn(node);
    }

    public void outATryStatement(ATryStatement node)
    {
        defaultOut(node);
    }

    @Override
    public void caseATryStatement(ATryStatement node)
    {
        inATryStatement(node);
        if(node.getTry() != null)
        {
            node.getTry().apply(this);
        }
        if(node.getLBrace() != null)
        {
            node.getLBrace().apply(this);
        }
        {
            List<PStatement> copy = new ArrayList<PStatement>(node.getStatement());
            for(PStatement e : copy)
            {
                e.apply(this);
            }
        }
        if(node.getRBrace() != null)
        {
            node.getRBrace().apply(this);
        }
        {
            List<PCatchBlock> copy = new ArrayList<PCatchBlock>(node.getCatchBlock());
            for(PCatchBlock e : copy)
            {
                e.apply(this);
            }
        }
        outATryStatement(node);
    }

    public void inAThrowStatement(AThrowStatement node)
    {
        defaultIn(node);
    }

    public void outAThrowStatement(AThrowStatement node)
    {
        defaultOut(node);
    }

    @Override
    public void caseAThrowStatement(AThrowStatement node)
    {
        inAThrowStatement(node);
        if(node.getThrow() != null)
        {
            node.getThrow().apply(this);
        }
        if(node.getName() != null)
        {
            node.getName().apply(this);
        }
        if(node.getSemi() != null)
        {
            node.getSemi().apply(this);
        }
        outAThrowStatement(node);
    }

    public void inAReturnStatement(AReturnStatement node)
    {
        defaultIn(node);
    }

    public void outAReturnStatement(AReturnStatement node)
    {
        defaultOut(node);
    }

    @Override
    public void caseAReturnStatement(AReturnStatement node)
    {
        inAReturnStatement(node);
        if(node.getReturn() != null)
        {
            node.getReturn().apply(this);
        }
        if(node.getName() != null)
        {
            node.getName().apply(this);
        }
        if(node.getSemi() != null)
        {
            node.getSemi().apply(this);
        }
        outAReturnStatement(node);
    }

    public void inACatchBlock(ACatchBlock node)
    {
        defaultIn(node);
    }

    public void outACatchBlock(ACatchBlock node)
    {
        defaultOut(node);
    }

    @Override
    public void caseACatchBlock(ACatchBlock node)
    {
        inACatchBlock(node);
        if(node.getCatch() != null)
        {
            node.getCatch().apply(this);
        }
        if(node.getLPar() != null)
        {
            node.getLPar().apply(this);
        }
        if(node.getType() != null)
        {
            node.getType().apply(this);
        }
        if(node.getName() != null)
        {
            node.getName().apply(this);
        }
        if(node.getRPar() != null)
        {
            node.getRPar().apply(this);
        }
        if(node.getLBrace() != null)
        {
            node.getLBrace().apply(this);
        }
        {
            List<PStatement> copy = new ArrayList<PStatement>(node.getStatement());
            for(PStatement e : copy)
            {
                e.apply(this);
            }
        }
        if(node.getRBrace() != null)
        {
            node.getRBrace().apply(this);
        }
        outACatchBlock(node);
    }

    public void inANewExpr(ANewExpr node)
    {
        defaultIn(node);
    }

    public void outANewExpr(ANewExpr node)
    {
        defaultOut(node);
    }

    @Override
    public void caseANewExpr(ANewExpr node)
    {
        inANewExpr(node);
        if(node.getNew() != null)
        {
            node.getNew().apply(this);
        }
        if(node.getType() != null)
        {
            node.getType().apply(this);
        }
        if(node.getLPar() != null)
        {
            node.getLPar().apply(this);
        }
        if(node.getArgs() != null)
        {
            node.getArgs().apply(this);
        }
        if(node.getStar() != null)
        {
            node.getStar().apply(this);
        }
        if(node.getRPar() != null)
        {
            node.getRPar().apply(this);
        }
        outANewExpr(node);
    }

    public void inACallExpr(ACallExpr node)
    {
        defaultIn(node);
    }

    public void outACallExpr(ACallExpr node)
    {
        defaultOut(node);
    }

    @Override
    public void caseACallExpr(ACallExpr node)
    {
        inACallExpr(node);
        if(node.getName() != null)
        {
            node.getName().apply(this);
        }
        if(node.getDot() != null)
        {
            node.getDot().apply(this);
        }
        if(node.getMethod() != null)
        {
            node.getMethod().apply(this);
        }
        if(node.getLPar() != null)
        {
            node.getLPar().apply(this);
        }
        if(node.getArgs() != null)
        {
            node.getArgs().apply(this);
        }
        if(node.getStar() != null)
        {
            node.getStar().apply(this);
        }
        if(node.getRPar() != null)
        {
            node.getRPar().apply(this);
        }
        outACallExpr(node);
    }

    public void inAStringExpr(AStringExpr node)
    {
        defaultIn(node);
    }

    public void outAStringExpr(AStringExpr node)
    {
        defaultOut(node);
    }

    @Override
    public void caseAStringExpr(AStringExpr node)
    {
        inAStringExpr(node);
        if(node.getStringLiteral() != null)
        {
            node.getStringLiteral().apply(this);
        }
        outAStringExpr(node);
    }

    public void inACopyExpr(ACopyExpr node)
    {
        defaultIn(node);
    }

    public void outACopyExpr(ACopyExpr node)
    {
        defaultOut(node);
    }

    @Override
    public void caseACopyExpr(ACopyExpr node)
    {
        inACopyExpr(node);
        if(node.getName() != null)
        {
            node.getName().apply(this);
        }
        outACopyExpr(node);
    }

    public void inAStringArgs(AStringArgs node)
    {
        defaultIn(node);
    }

    public void outAStringArgs(AStringArgs node)
    {
        defaultOut(node);
    }

    @Override
    public void caseAStringArgs(AStringArgs node)
    {
        inAStringArgs(node);
        if(node.getStringLiteral() != null)
        {
            node.getStringLiteral().apply(this);
        }
        {
            List<PStringArgsTail> copy = new ArrayList<PStringArgsTail>(node.getStringArgsTail());
            for(PStringArgsTail e : copy)
            {
                e.apply(this);
            }
        }
        outAStringArgs(node);
    }

    public void inAStringArgsTail(AStringArgsTail node)
    {
        defaultIn(node);
    }

    public void outAStringArgsTail(AStringArgsTail node)
    {
        defaultOut(node);
    }

    @Override
    public void caseAStringArgsTail(AStringArgsTail node)
    {
        inAStringArgsTail(node);
        if(node.getComma() != null)
        {
            node.getComma().apply(this);
        }
        if(node.getStringLiteral() != null)
        {
            node.getStringLiteral().apply(this);
        }
        outAStringArgsTail(node);
    }

    public void inAArgs(AArgs node)
    {
        defaultIn(node);
    }

    public void outAArgs(AArgs node)
    {
        defaultOut(node);
    }

    @Override
    public void caseAArgs(AArgs node)
    {
        inAArgs(node);
        if(node.getExpr() != null)
        {
            node.getExpr().apply(this);
        }
        {
            List<PArgsTail> copy = new ArrayList<PArgsTail>(node.getArgsTail());
            for(PArgsTail e : copy)
            {
                e.apply(this);
            }
        }
        outAArgs(node);
    }

    public void inAArgsTail(AArgsTail node)
    {
        defaultIn(node);
    }

    public void outAArgsTail(AArgsTail node)
    {
        defaultOut(node);
    }

    @Override
    public void caseAArgsTail(AArgsTail node)
    {
        inAArgsTail(node);
        if(node.getComma() != null)
        {
            node.getComma().apply(this);
        }
        if(node.getExpr() != null)
        {
            node.getExpr().apply(this);
        }
        outAArgsTail(node);
    }

    public void inAParam(AParam node)
    {
        defaultIn(node);
    }

    public void outAParam(AParam node)
    {
        defaultOut(node);
    }

    @Override
    public void caseAParam(AParam node)
    {
        inAParam(node);
        if(node.getType() != null)
        {
            node.getType().apply(this);
        }
        if(node.getName() != null)
        {
            node.getName().apply(this);
        }
        outAParam(node);
    }

    public void inAParams(AParams node)
    {
        defaultIn(node);
    }

    public void outAParams(AParams node)
    {
        defaultOut(node);
    }

    @Override
    public void caseAParams(AParams node)
    {
        inAParams(node);
        if(node.getParam() != null)
        {
            node.getParam().apply(this);
        }
        {
            List<PParamsTail> copy = new ArrayList<PParamsTail>(node.getParamsTail());
            for(PParamsTail e : copy)
            {
                e.apply(this);
            }
        }
        outAParams(node);
    }

    public void inAParamsTail(AParamsTail node)
    {
        defaultIn(node);
    }

    public void outAParamsTail(AParamsTail node)
    {
        defaultOut(node);
    }

    @Override
    public void caseAParamsTail(AParamsTail node)
    {
        inAParamsTail(node);
        if(node.getComma() != null)
        {
            node.getComma().apply(this);
        }
        if(node.getParam() != null)
        {
            node.getParam().apply(this);
        }
        outAParamsTail(node);
    }

    public void inANamedblock(ANamedblock node)
    {
        defaultIn(node);
    }

    public void outANamedblock(ANamedblock node)
    {
        defaultOut(node);
    }

    @Override
    public void caseANamedblock(ANamedblock node)
    {
        inANamedblock(node);
        if(node.getName() != null)
        {
            node.getName().apply(this);
        }
        if(node.getContext() != null)
        {
            node.getContext().apply(this);
        }
        if(node.getLBrace() != null)
        {
            node.getLBrace().apply(this);
        }
        if(node.getCode() != null)
        {
            node.getCode().apply(this);
        }
        if(node.getRBrace() != null)
        {
            node.getRBrace().apply(this);
        }
        outANamedblock(node);
    }
}
