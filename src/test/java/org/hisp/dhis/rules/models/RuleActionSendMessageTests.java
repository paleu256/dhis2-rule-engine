package org.hisp.dhis.rules.models;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith( JUnit4.class )
public class RuleActionSendMessageTests
{

        @Test
        public void substitute_empty_strings_when_create_with_null_arguments()
        {
                RuleActionSendMessage ruleActionSendMessage = RuleActionSendMessage.Companion.create( "notification", "data" );
                RuleActionSendMessage ruleActionSendMessageNoData = RuleActionSendMessage.Companion
                    .create( "notification", null );
                RuleActionSendMessage ruleActionSendMessageNoNotification = RuleActionSendMessage.Companion
                    .create( null, "data" );

                assertThat( ruleActionSendMessage.notification() ).isEqualTo( "notification" );
                assertThat( ruleActionSendMessage.data() ).isEqualTo( "data" );

                assertThat( ruleActionSendMessageNoData.notification() ).isEqualTo( "notification" );
                assertThat( ruleActionSendMessageNoData.data() ).isEqualTo( "" );

                assertThat( ruleActionSendMessageNoNotification.notification() ).isEqualTo( "" );
                assertThat( ruleActionSendMessageNoNotification.data() ).isEqualTo( "data" );
        }
}
