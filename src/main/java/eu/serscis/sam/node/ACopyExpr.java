/* This file was generated by SableCC (http://www.sablecc.org/). */

package eu.serscis.sam.node;

import eu.serscis.sam.analysis.*;

@SuppressWarnings("nls")
public final class ACopyExpr extends PExpr
{
    private TName _name_;

    public ACopyExpr()
    {
        // Constructor
    }

    public ACopyExpr(
        @SuppressWarnings("hiding") TName _name_)
    {
        // Constructor
        setName(_name_);

    }

    @Override
    public Object clone()
    {
        return new ACopyExpr(
            cloneNode(this._name_));
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseACopyExpr(this);
    }

    public TName getName()
    {
        return this._name_;
    }

    public void setName(TName node)
    {
        if(this._name_ != null)
        {
            this._name_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._name_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._name_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._name_ == child)
        {
            this._name_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._name_ == oldChild)
        {
            setName((TName) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}
