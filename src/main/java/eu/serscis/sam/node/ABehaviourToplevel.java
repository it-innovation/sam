/* This file was generated by SableCC (http://www.sablecc.org/). */

package eu.serscis.sam.node;

import eu.serscis.sam.analysis.*;

@SuppressWarnings("nls")
public final class ABehaviourToplevel extends PToplevel
{
    private PBehaviour _behaviour_;

    public ABehaviourToplevel()
    {
        // Constructor
    }

    public ABehaviourToplevel(
        @SuppressWarnings("hiding") PBehaviour _behaviour_)
    {
        // Constructor
        setBehaviour(_behaviour_);

    }

    @Override
    public Object clone()
    {
        return new ABehaviourToplevel(
            cloneNode(this._behaviour_));
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseABehaviourToplevel(this);
    }

    public PBehaviour getBehaviour()
    {
        return this._behaviour_;
    }

    public void setBehaviour(PBehaviour node)
    {
        if(this._behaviour_ != null)
        {
            this._behaviour_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._behaviour_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._behaviour_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._behaviour_ == child)
        {
            this._behaviour_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._behaviour_ == oldChild)
        {
            setBehaviour((PBehaviour) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}
