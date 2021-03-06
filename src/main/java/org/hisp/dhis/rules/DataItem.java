package org.hisp.dhis.rules;

/*
 * Copyright (c) 2004-2020, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * Class is place holder for program rule variable, Constant and program environment variable display name and type.
 * @author Zubair Asghar
 */
public class DataItem
{
    private String displayName;

    private ItemValueType valueType;

    public DataItem( String value, ItemValueType valueType )
    {
        this.displayName = value;
        this.valueType = valueType;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public ItemValueType getValueType()
    {
        return valueType;
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder
    {
        private String displayName;
        private ItemValueType itemValueType;

        public Builder value( String value )
        {
            this.displayName = value;
            return this;
        }

        public Builder valueType( ItemValueType valueType )
        {
            this.itemValueType = valueType;
            return this;
        }

        public DataItem build()
        {
            return new DataItem( displayName, itemValueType );
        }
    }
}
