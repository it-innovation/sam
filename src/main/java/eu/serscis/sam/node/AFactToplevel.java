/* This file was generated by SableCC (http://www.sablecc.org/). */

package eu.serscis.sam.node;

import eu.serscis.sam.analysis.*;

@SuppressWarnings("nls")
public final class AFactToplevel extends PToplevel
{
    private PFact _fact_;

    public AFactToplevel()
    {
        // Constructor
    }

    public AFactToplevel(
        @SuppressWarnings("hiding") PFact _fact_)
    {
        // Constructor
        setFact(_fact_);

    }

    @Override
    public Object clone()
    {
        return new AFactToplevel(
            cloneNode(this._fact_));
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseAFactToplevel(this);
    }

    public PFact getFact()
    {
        return this._fact_;
    }

    public void setFact(PFact node)
    {
        if(this._fact_ != null)
        {
            this._fact_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._fact_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._fact_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._fact_ == child)
        {
            this._fact_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._fact_ == oldChild)
        {
            setFact((PFact) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}
