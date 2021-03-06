package org.hisp.dhis.rules.models;

import javax.annotation.Nonnull;

public enum RuleValueType
{
    TEXT( "" ), NUMERIC( "0.0" ), BOOLEAN( "false" ), DATE( "2020-01-01" );

    @Nonnull
    private final String defaultValue;

    RuleValueType( @Nonnull String defaultValue )
    {
        this.defaultValue = defaultValue;
    }

    @Nonnull
    public String defaultValue()
    {
        return defaultValue;
    }
}
