/* This file was generated by SableCC (http://www.sablecc.org/). */

package eu.serscis.sam.node;

import eu.serscis.sam.analysis.*;

@SuppressWarnings("nls")
public final class TDots extends Token
{
    public TDots()
    {
        super.setText("...");
    }

    public TDots(int line, int pos)
    {
        super.setText("...");
        setLine(line);
        setPos(pos);
    }

    @Override
    public Object clone()
    {
      return new TDots(getLine(), getPos());
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseTDots(this);
    }

    @Override
    public void setText(@SuppressWarnings("unused") String text)
    {
        throw new RuntimeException("Cannot change TDots text.");
    }
}