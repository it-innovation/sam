/* This file was generated by SableCC (http://www.sablecc.org/). */

package eu.serscis.sam.node;

import java.util.*;
import eu.serscis.sam.analysis.*;

@SuppressWarnings("nls")
public final class AArgs extends PArgs
{
    private TName _name_;
    private final LinkedList<PArgsTail> _argsTail_ = new LinkedList<PArgsTail>();

    public AArgs()
    {
        // Constructor
    }

    public AArgs(
        @SuppressWarnings("hiding") TName _name_,
        @SuppressWarnings("hiding") List<PArgsTail> _argsTail_)
    {
        // Constructor
        setName(_name_);

        setArgsTail(_argsTail_);

    }

    @Override
    public Object clone()
    {
        return new AArgs(
            cloneNode(this._name_),
            cloneList(this._argsTail_));
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseAArgs(this);
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

    public LinkedList<PArgsTail> getArgsTail()
    {
        return this._argsTail_;
    }

    public void setArgsTail(List<PArgsTail> list)
    {
        this._argsTail_.clear();
        this._argsTail_.addAll(list);
        for(PArgsTail e : list)
        {
            if(e.parent() != null)
            {
                e.parent().removeChild(e);
            }

            e.parent(this);
        }
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._name_)
            + toString(this._argsTail_);
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

        if(this._argsTail_.remove(child))
        {
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

        for(ListIterator<PArgsTail> i = this._argsTail_.listIterator(); i.hasNext();)
        {
            if(i.next() == oldChild)
            {
                if(newChild != null)
                {
                    i.set((PArgsTail) newChild);
                    newChild.parent(this);
                    oldChild.parent(null);
                    return;
                }

                i.remove();
                oldChild.parent(null);
                return;
            }
        }

        throw new RuntimeException("Not a child.");
    }
}