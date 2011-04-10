/* This file was generated by SableCC (http://www.sablecc.org/). */

package eu.serscis.sam.node;

import eu.serscis.sam.analysis.*;

@SuppressWarnings("nls")
public final class ANormalAtom extends PAtom
{
    private TName _name_;
    private TLPar _lPar_;
    private PTerms _terms_;
    private TRPar _rPar_;

    public ANormalAtom()
    {
        // Constructor
    }

    public ANormalAtom(
        @SuppressWarnings("hiding") TName _name_,
        @SuppressWarnings("hiding") TLPar _lPar_,
        @SuppressWarnings("hiding") PTerms _terms_,
        @SuppressWarnings("hiding") TRPar _rPar_)
    {
        // Constructor
        setName(_name_);

        setLPar(_lPar_);

        setTerms(_terms_);

        setRPar(_rPar_);

    }

    @Override
    public Object clone()
    {
        return new ANormalAtom(
            cloneNode(this._name_),
            cloneNode(this._lPar_),
            cloneNode(this._terms_),
            cloneNode(this._rPar_));
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseANormalAtom(this);
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

    public TLPar getLPar()
    {
        return this._lPar_;
    }

    public void setLPar(TLPar node)
    {
        if(this._lPar_ != null)
        {
            this._lPar_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._lPar_ = node;
    }

    public PTerms getTerms()
    {
        return this._terms_;
    }

    public void setTerms(PTerms node)
    {
        if(this._terms_ != null)
        {
            this._terms_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._terms_ = node;
    }

    public TRPar getRPar()
    {
        return this._rPar_;
    }

    public void setRPar(TRPar node)
    {
        if(this._rPar_ != null)
        {
            this._rPar_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._rPar_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._name_)
            + toString(this._lPar_)
            + toString(this._terms_)
            + toString(this._rPar_);
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

        if(this._lPar_ == child)
        {
            this._lPar_ = null;
            return;
        }

        if(this._terms_ == child)
        {
            this._terms_ = null;
            return;
        }

        if(this._rPar_ == child)
        {
            this._rPar_ = null;
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

        if(this._lPar_ == oldChild)
        {
            setLPar((TLPar) newChild);
            return;
        }

        if(this._terms_ == oldChild)
        {
            setTerms((PTerms) newChild);
            return;
        }

        if(this._rPar_ == oldChild)
        {
            setRPar((TRPar) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}
