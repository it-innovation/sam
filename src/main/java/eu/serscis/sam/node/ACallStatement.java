/* This file was generated by SableCC (http://www.sablecc.org/). */

package eu.serscis.sam.node;

import eu.serscis.sam.analysis.*;

@SuppressWarnings("nls")
public final class ACallStatement extends PStatement
{
    private PAssign _assign_;
    private PCallExpr _callExpr_;
    private TSemi _semi_;

    public ACallStatement()
    {
        // Constructor
    }

    public ACallStatement(
        @SuppressWarnings("hiding") PAssign _assign_,
        @SuppressWarnings("hiding") PCallExpr _callExpr_,
        @SuppressWarnings("hiding") TSemi _semi_)
    {
        // Constructor
        setAssign(_assign_);

        setCallExpr(_callExpr_);

        setSemi(_semi_);

    }

    @Override
    public Object clone()
    {
        return new ACallStatement(
            cloneNode(this._assign_),
            cloneNode(this._callExpr_),
            cloneNode(this._semi_));
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseACallStatement(this);
    }

    public PAssign getAssign()
    {
        return this._assign_;
    }

    public void setAssign(PAssign node)
    {
        if(this._assign_ != null)
        {
            this._assign_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._assign_ = node;
    }

    public PCallExpr getCallExpr()
    {
        return this._callExpr_;
    }

    public void setCallExpr(PCallExpr node)
    {
        if(this._callExpr_ != null)
        {
            this._callExpr_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._callExpr_ = node;
    }

    public TSemi getSemi()
    {
        return this._semi_;
    }

    public void setSemi(TSemi node)
    {
        if(this._semi_ != null)
        {
            this._semi_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._semi_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._assign_)
            + toString(this._callExpr_)
            + toString(this._semi_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._assign_ == child)
        {
            this._assign_ = null;
            return;
        }

        if(this._callExpr_ == child)
        {
            this._callExpr_ = null;
            return;
        }

        if(this._semi_ == child)
        {
            this._semi_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._assign_ == oldChild)
        {
            setAssign((PAssign) newChild);
            return;
        }

        if(this._callExpr_ == oldChild)
        {
            setCallExpr((PCallExpr) newChild);
            return;
        }

        if(this._semi_ == oldChild)
        {
            setSemi((TSemi) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}
