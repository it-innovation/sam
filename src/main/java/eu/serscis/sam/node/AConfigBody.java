/* This file was generated by SableCC (http://www.sablecc.org/). */

package eu.serscis.sam.node;

import java.util.*;
import eu.serscis.sam.analysis.*;

@SuppressWarnings("nls")
public final class AConfigBody extends PConfigBody
{
    private final LinkedList<PConfigField> _configField_ = new LinkedList<PConfigField>();
    private final LinkedList<PNamedblock> _namedblock_ = new LinkedList<PNamedblock>();

    public AConfigBody()
    {
        // Constructor
    }

    public AConfigBody(
        @SuppressWarnings("hiding") List<PConfigField> _configField_,
        @SuppressWarnings("hiding") List<PNamedblock> _namedblock_)
    {
        // Constructor
        setConfigField(_configField_);

        setNamedblock(_namedblock_);

    }

    @Override
    public Object clone()
    {
        return new AConfigBody(
            cloneList(this._configField_),
            cloneList(this._namedblock_));
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseAConfigBody(this);
    }

    public LinkedList<PConfigField> getConfigField()
    {
        return this._configField_;
    }

    public void setConfigField(List<PConfigField> list)
    {
        this._configField_.clear();
        this._configField_.addAll(list);
        for(PConfigField e : list)
        {
            if(e.parent() != null)
            {
                e.parent().removeChild(e);
            }

            e.parent(this);
        }
    }

    public LinkedList<PNamedblock> getNamedblock()
    {
        return this._namedblock_;
    }

    public void setNamedblock(List<PNamedblock> list)
    {
        this._namedblock_.clear();
        this._namedblock_.addAll(list);
        for(PNamedblock e : list)
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
            + toString(this._configField_)
            + toString(this._namedblock_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._configField_.remove(child))
        {
            return;
        }

        if(this._namedblock_.remove(child))
        {
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        for(ListIterator<PConfigField> i = this._configField_.listIterator(); i.hasNext();)
        {
            if(i.next() == oldChild)
            {
                if(newChild != null)
                {
                    i.set((PConfigField) newChild);
                    newChild.parent(this);
                    oldChild.parent(null);
                    return;
                }

                i.remove();
                oldChild.parent(null);
                return;
            }
        }

        for(ListIterator<PNamedblock> i = this._namedblock_.listIterator(); i.hasNext();)
        {
            if(i.next() == oldChild)
            {
                if(newChild != null)
                {
                    i.set((PNamedblock) newChild);
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
