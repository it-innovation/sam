/* This file was generated by SableCC (http://www.sablecc.org/). */

package eu.serscis.sam.node;

import eu.serscis.sam.analysis.*;

@SuppressWarnings("nls")
public final class AMethod extends PMethod
{
    private TPublicTok _publicTok_;
    private PType _type_;
    private TName _name_;
    private TLPar _lPar_;
    private TRPar _rPar_;
    private TLBrace _lBrace_;
    private PCode _code_;
    private TRBrace _rBrace_;

    public AMethod()
    {
        // Constructor
    }

    public AMethod(
        @SuppressWarnings("hiding") TPublicTok _publicTok_,
        @SuppressWarnings("hiding") PType _type_,
        @SuppressWarnings("hiding") TName _name_,
        @SuppressWarnings("hiding") TLPar _lPar_,
        @SuppressWarnings("hiding") TRPar _rPar_,
        @SuppressWarnings("hiding") TLBrace _lBrace_,
        @SuppressWarnings("hiding") PCode _code_,
        @SuppressWarnings("hiding") TRBrace _rBrace_)
    {
        // Constructor
        setPublicTok(_publicTok_);

        setType(_type_);

        setName(_name_);

        setLPar(_lPar_);

        setRPar(_rPar_);

        setLBrace(_lBrace_);

        setCode(_code_);

        setRBrace(_rBrace_);

    }

    @Override
    public Object clone()
    {
        return new AMethod(
            cloneNode(this._publicTok_),
            cloneNode(this._type_),
            cloneNode(this._name_),
            cloneNode(this._lPar_),
            cloneNode(this._rPar_),
            cloneNode(this._lBrace_),
            cloneNode(this._code_),
            cloneNode(this._rBrace_));
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseAMethod(this);
    }

    public TPublicTok getPublicTok()
    {
        return this._publicTok_;
    }

    public void setPublicTok(TPublicTok node)
    {
        if(this._publicTok_ != null)
        {
            this._publicTok_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._publicTok_ = node;
    }

    public PType getType()
    {
        return this._type_;
    }

    public void setType(PType node)
    {
        if(this._type_ != null)
        {
            this._type_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._type_ = node;
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

    public TLBrace getLBrace()
    {
        return this._lBrace_;
    }

    public void setLBrace(TLBrace node)
    {
        if(this._lBrace_ != null)
        {
            this._lBrace_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._lBrace_ = node;
    }

    public PCode getCode()
    {
        return this._code_;
    }

    public void setCode(PCode node)
    {
        if(this._code_ != null)
        {
            this._code_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._code_ = node;
    }

    public TRBrace getRBrace()
    {
        return this._rBrace_;
    }

    public void setRBrace(TRBrace node)
    {
        if(this._rBrace_ != null)
        {
            this._rBrace_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._rBrace_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._publicTok_)
            + toString(this._type_)
            + toString(this._name_)
            + toString(this._lPar_)
            + toString(this._rPar_)
            + toString(this._lBrace_)
            + toString(this._code_)
            + toString(this._rBrace_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._publicTok_ == child)
        {
            this._publicTok_ = null;
            return;
        }

        if(this._type_ == child)
        {
            this._type_ = null;
            return;
        }

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

        if(this._rPar_ == child)
        {
            this._rPar_ = null;
            return;
        }

        if(this._lBrace_ == child)
        {
            this._lBrace_ = null;
            return;
        }

        if(this._code_ == child)
        {
            this._code_ = null;
            return;
        }

        if(this._rBrace_ == child)
        {
            this._rBrace_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._publicTok_ == oldChild)
        {
            setPublicTok((TPublicTok) newChild);
            return;
        }

        if(this._type_ == oldChild)
        {
            setType((PType) newChild);
            return;
        }

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

        if(this._rPar_ == oldChild)
        {
            setRPar((TRPar) newChild);
            return;
        }

        if(this._lBrace_ == oldChild)
        {
            setLBrace((TLBrace) newChild);
            return;
        }

        if(this._code_ == oldChild)
        {
            setCode((PCode) newChild);
            return;
        }

        if(this._rBrace_ == oldChild)
        {
            setRBrace((TRBrace) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}
