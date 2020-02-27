package org.hisp.dhis.rules;

import org.hisp.dhis.rules.models.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.hisp.dhis.rules.RuleVariableValue.create;

@SuppressWarnings( "PMD.GodClass" )
final class RuleVariableValueMapBuilder
{
    private static final String DATE_PATTERN = "yyyy-MM-dd";

    private static final String ENV_VAR_CURRENT_DATE = "current_date";

    private static final String ENV_VAR_EVENT_DATE = "event_date";

    private static final String ENV_VAR_EVENT_COUNT = "event_count";

    private static final String ENV_VAR_DUE_DATE = "due_date";

    private static final String ENV_VAR_EVENT_ID = "event_id";

    private static final String ENV_VAR_ENROLLMENT_DATE = "enrollment_date";

    private static final String ENV_VAR_ENROLLMENT_ID = "enrollment_id";

    private static final String ENV_VAR_ENROLLMENT_COUNT = "enrollment_count";

    private static final String ENV_VAR_INCIDENT_DATE = "incident_date";

    private static final String ENV_VAR_TEI_COUNT = "tei_count";

    private static final String ENV_VAR_EVENT_STATUS = "event_status";

    private static final String ENV_VAR_OU = "org_unit";

    private static final String ENV_VAR_ENROLLMENT_STATUS = "enrollment_status";

    private static final String ENV_VAR_PROGRAM_STAGE_ID = "program_stage_id";

    private static final String ENV_VAR_PROGRAM_STAGE_NAME = "program_stage_name";

    private static final String ENV_VAR_PROGRAM_NAME = "program_name";

    private static final String ENV_VAR_ENVIRONMENT = "environment";

    private static final String ENV_VAR_OU_CODE = "orgunit_code";

    @Nonnull
    private final SimpleDateFormat dateFormat;

    @Nonnull
    private final Map<String, RuleDataValue> currentEventValues;

    @Nonnull
    private final Map<String, RuleAttributeValue> currentEnrollmentValues;

    @Nonnull
    private final Map<String, List<RuleDataValue>> allEventsValues;

    @Nonnull
    private final Map<String, Map<String, String>> calculatedValueMap;

    @Nonnull
    private final Map<String, String> allConstantValues;

    @Nonnull
    private final List<RuleVariable> ruleVariables;

    @Nonnull
    private final List<RuleEvent> ruleEvents;

    @Nullable
    private RuleEnrollment ruleEnrollment;

    @Nullable
    private RuleEvent ruleEvent;

    @Nullable
    private TriggerEnvironment triggerEnvironment;

    private RuleVariableValueMapBuilder()
    {
        this.dateFormat = new SimpleDateFormat( DATE_PATTERN, Locale.US );

        // collections used for construction of resulting variable value map
        this.currentEnrollmentValues = new HashMap<>();
        this.currentEventValues = new HashMap<>();
        this.allEventsValues = new HashMap<>();
        this.ruleVariables = new ArrayList<>();
        this.ruleEvents = new ArrayList<>();
        this.calculatedValueMap = new HashMap<>();
        this.allConstantValues = new HashMap<>();
    }

    private RuleVariableValueMapBuilder( @Nonnull RuleEnrollment ruleEnrollment )
    {
        this();

        // enrollment is the target
        this.ruleEnrollment = ruleEnrollment;
    }

    private RuleVariableValueMapBuilder( @Nonnull RuleEvent ruleEvent )
    {
        this();

        // event is the target
        this.ruleEvent = ruleEvent;
    }

    @Nonnull
    static RuleVariableValueMapBuilder target( @Nonnull RuleEnrollment ruleEnrollment )
    {
        return new RuleVariableValueMapBuilder( ruleEnrollment );
    }

    @Nonnull
    static RuleVariableValueMapBuilder target( @Nonnull RuleEvent ruleEvent )
    {
        return new RuleVariableValueMapBuilder( ruleEvent );
    }

    private static boolean isEventInList( @Nonnull List<RuleEvent> ruleEvents,
        @Nullable RuleEvent ruleEvent )
    {
        if ( ruleEvent != null )
        {
            for ( int i = 0; i < ruleEvents.size(); i++ )
            {
                RuleEvent event = ruleEvents.get( i );

                if ( event.event().equals( ruleEvent.event() ) )
                {
                    return true;
                }
            }
        }

        return false;
    }

    @Nonnull
    RuleVariableValueMapBuilder ruleVariables( @Nonnull List<RuleVariable> ruleVariables )
    {
        this.ruleVariables.addAll( ruleVariables );
        return this;
    }

    @Nonnull
    RuleVariableValueMapBuilder ruleEnrollment( @Nullable RuleEnrollment ruleEnrollment )
    {
        if ( this.ruleEnrollment != null )
        {
            throw new IllegalStateException( "It seems that enrollment has been set as target " +
                "already. It can't be used as a part of execution context." );
        }

        this.ruleEnrollment = ruleEnrollment;
        return this;
    }

    @Nonnull
    RuleVariableValueMapBuilder triggerEnvironment( @Nullable TriggerEnvironment triggerEnvironment )
    {
        if ( this.triggerEnvironment != null )
        {
            throw new IllegalStateException( "triggerEnvironment == null" );
        }

        this.triggerEnvironment = triggerEnvironment;
        return this;
    }

    @Nonnull
    RuleVariableValueMapBuilder ruleEvents( @Nonnull List<RuleEvent> ruleEvents )
    {
        if ( isEventInList( ruleEvents, ruleEvent ) )
        {
            throw new IllegalStateException( String.format( Locale.US, "ruleEvent %s is already set " +
                "as a target, but also present in the context: ruleEvents list", ruleEvent.event() ) );
        }

        this.ruleEvents.addAll( ruleEvents );
        return this;
    }

    @Nonnull
    RuleVariableValueMapBuilder calculatedValueMap( @Nonnull Map<String, Map<String, String>> calculatedValueMap )
    {
        this.calculatedValueMap.putAll( calculatedValueMap );
        return this;
    }

    @Nonnull
    RuleVariableValueMapBuilder constantValueMap( @Nonnull Map<String, String> constantValues )
    {
        this.allConstantValues.putAll( constantValues );
        return this;
    }

    @Nonnull
    Map<String, RuleVariableValue> build()
    {
        Map<String, RuleVariableValue> valueMap = new HashMap<>();

        // map tracked entity attributes to values from enrollment
        buildCurrentEnrollmentValues();

        // build a map of current event values
        buildCurrentEventValues();

        // map data values within all events to data elements
        buildAllEventValues();

        // set environment variables
        buildEnvironmentVariables( valueMap );

        // set metadata variables
        buildRuleVariableValues( valueMap );

        // set constants value map
        buildConstantsValues( valueMap );

        // do not let outer world to alter variable value map
        return Collections.unmodifiableMap( valueMap );
    }

    private void buildCurrentEventValues()
    {
        if ( ruleEvent != null )
        {
            for ( int index = 0; index < ruleEvent.dataValues().size(); index++ )
            {
                RuleDataValue ruleDataValue = ruleEvent.dataValues().get( index );
                currentEventValues.put( ruleDataValue.dataElement(), ruleDataValue );
            }
        }
    }

    private void buildCurrentEnrollmentValues()
    {
        if ( ruleEnrollment != null )
        {
            List<RuleAttributeValue> ruleAttributeValues = ruleEnrollment.attributeValues();
            for ( int index = 0; index < ruleAttributeValues.size(); index++ )
            {
                RuleAttributeValue attributeValue = ruleAttributeValues.get( index );
                currentEnrollmentValues.put( attributeValue.trackedEntityAttribute(), attributeValue );
            }
        }
    }

    private void buildAllEventValues()
    {
        List<RuleEvent> events = new ArrayList<>( ruleEvents );

        if ( ruleEvent != null )
        {
            // target event should be among the list of all
            // events in order to achieve correct behavior
            events.add( ruleEvent );
        }

        // sort list of events by eventDate:
        Collections.sort( events, RuleEvent.EVENT_DATE_COMPARATOR );

        // aggregating values by data element uid
        for ( int i = 0; i < events.size(); i++ )
        {
            RuleEvent ruleEvent = events.get( i );

            for ( int j = 0; j < ruleEvent.dataValues().size(); j++ )
            {
                RuleDataValue ruleDataValue = ruleEvent.dataValues().get( j );

                // push new list if it is not there for the given data element
                if ( !allEventsValues.containsKey( ruleDataValue.dataElement() ) )
                {
                    allEventsValues.put( ruleDataValue.dataElement(),
                        new ArrayList<RuleDataValue>( events.size() ) ); //NOPMD
                }

                // append data value to the list
                allEventsValues.get( ruleDataValue.dataElement() ).add( ruleDataValue );
            }
        }
    }

    private void buildConstantsValues( Map<String, RuleVariableValue> valueMap )
    {
        for ( Map.Entry<String, String> entrySet : allConstantValues.entrySet() )
        {
            valueMap.put( entrySet.getKey(), create( entrySet.getValue(), RuleValueType.NUMERIC ) );
        }
    }

    private void buildEnvironmentVariables( @Nonnull Map<String, RuleVariableValue> valueMap )
    {
        String currentDate = dateFormat.format( new Date() );
        valueMap.put( ENV_VAR_CURRENT_DATE,
            create( currentDate, RuleValueType.TEXT, Arrays.asList( currentDate ), currentDate ) );

        if ( triggerEnvironment != null )
        {
            String environment = triggerEnvironment.getClientName();
            valueMap.put( ENV_VAR_ENVIRONMENT,
                create( environment, RuleValueType.TEXT, Arrays.asList( environment ), currentDate ) );
        }

        if ( !ruleEvents.isEmpty() )
        {
            valueMap.put( ENV_VAR_EVENT_COUNT, create( String.valueOf( ruleEvents.size() ),
                RuleValueType.NUMERIC, Arrays.asList( String.valueOf( ruleEvents.size() ) ), currentDate ) );
        }

        if ( ruleEnrollment != null )
        {
            valueMap.put( ENV_VAR_ENROLLMENT_ID, create( ruleEnrollment.enrollment(),
                RuleValueType.TEXT, Arrays.asList( ruleEnrollment.enrollment() ), currentDate ) );
            valueMap.put( ENV_VAR_ENROLLMENT_COUNT, create( "1",
                RuleValueType.NUMERIC, Arrays.asList( "1" ), currentDate ) );
            valueMap.put( ENV_VAR_TEI_COUNT, create( "1",
                RuleValueType.NUMERIC, Arrays.asList( "1" ), currentDate ) );

            String enrollmentDate = dateFormat.format( ruleEnrollment.enrollmentDate() );
            valueMap.put( ENV_VAR_ENROLLMENT_DATE, create( enrollmentDate,
                RuleValueType.TEXT, Arrays.asList( enrollmentDate ), currentDate ) );

            String incidentDate = dateFormat.format( ruleEnrollment.incidentDate() );
            valueMap.put( ENV_VAR_INCIDENT_DATE, create( incidentDate,
                RuleValueType.TEXT, Arrays.asList( incidentDate ), currentDate ) );

            String status = ruleEnrollment.status().toString();
            valueMap.put( ENV_VAR_ENROLLMENT_STATUS, create( status,
                RuleValueType.TEXT, Arrays.asList( status ), currentDate ) );

            String organisationUnit = ruleEnrollment.organisationUnit();
            valueMap.put( ENV_VAR_OU, create( organisationUnit, RuleValueType.TEXT ) );

            String programName = ruleEnrollment.programName();
            valueMap.put( ENV_VAR_PROGRAM_NAME, create( programName, RuleValueType.TEXT ) );

            String organisationUnitCode = ruleEnrollment.organisationUnitCode();
            valueMap.put( ENV_VAR_OU_CODE, create( organisationUnitCode, RuleValueType.TEXT ) );
        }

        if ( ruleEvent != null )
        {
            String eventDate = dateFormat.format( ruleEvent.eventDate() );
            valueMap.put( ENV_VAR_EVENT_DATE, create( eventDate, RuleValueType.TEXT,
                Arrays.asList( eventDate ), currentDate ) );

            String dueDate = dateFormat.format( ruleEvent.dueDate() );
            valueMap.put( ENV_VAR_DUE_DATE, create( dueDate, RuleValueType.TEXT,
                Arrays.asList( dueDate ), currentDate ) );

            // override value of event count
            String eventCount = String.valueOf( ruleEvents.size() + 1 );
            valueMap.put( ENV_VAR_EVENT_COUNT, create( eventCount,
                RuleValueType.NUMERIC, Arrays.asList( eventCount ), currentDate ) );
            valueMap.put( ENV_VAR_EVENT_ID, create( ruleEvent.event(),
                RuleValueType.TEXT, Arrays.asList( ruleEvent.event() ), currentDate ) );

            String status = ruleEvent.status().toString();
            valueMap.put( ENV_VAR_EVENT_STATUS, create( status,
                RuleValueType.TEXT, Arrays.asList( status ), currentDate ) );

            String organisationUnit = ruleEvent.organisationUnit();
            valueMap.put( ENV_VAR_OU, create( organisationUnit, RuleValueType.TEXT ) );

            String programStageId = ruleEvent.programStage();
            valueMap.put( ENV_VAR_PROGRAM_STAGE_ID, create( programStageId, RuleValueType.TEXT ) );

            String programStageName = ruleEvent.programStageName();
            valueMap.put( ENV_VAR_PROGRAM_STAGE_NAME, create( programStageName, RuleValueType.TEXT ) );

            String organisationUnitCode = ruleEvent.organisationUnitCode();
            valueMap.put( ENV_VAR_OU_CODE, create( organisationUnitCode, RuleValueType.TEXT ) );
        }
    }

    private void buildRuleVariableValues( @Nonnull Map<String, RuleVariableValue> valueMap )
    {
        for ( RuleVariable ruleVariable : ruleVariables )
        {
            if ( ruleVariable instanceof RuleVariableAttribute )
            {
                RuleVariableAttribute ruleVariableAttribute
                    = (RuleVariableAttribute) ruleVariable;
                createAttributeVariableValue( valueMap, ruleVariableAttribute );
            }
            else if ( ruleVariable instanceof RuleVariableCurrentEvent )
            {
                RuleVariableCurrentEvent currentEventVariable
                    = (RuleVariableCurrentEvent) ruleVariable;
                createCurrentEventVariableValue( valueMap, currentEventVariable );
            }
            else if ( ruleVariable instanceof RuleVariablePreviousEvent )
            {
                RuleVariablePreviousEvent ruleVariablePreviousEvent
                    = (RuleVariablePreviousEvent) ruleVariable;
                createPreviousEventVariableValue( valueMap, ruleVariablePreviousEvent );
            }
            else if ( ruleVariable instanceof RuleVariableNewestEvent )
            {
                RuleVariableNewestEvent ruleVariableNewestEvent
                    = (RuleVariableNewestEvent) ruleVariable;
                createNewestEventVariableValue( valueMap, ruleVariableNewestEvent );
            }
            else if ( ruleVariable instanceof RuleVariableNewestStageEvent )
            {
                RuleVariableNewestStageEvent ruleVariableNewestEvent
                    = (RuleVariableNewestStageEvent) ruleVariable;
                createNewestStageEventVariableValue( valueMap, ruleVariableNewestEvent );
            }
            else if ( ruleVariable instanceof RuleVariableCalculatedValue )
            {
                RuleVariableCalculatedValue ruleVariableCalculatedValue = (RuleVariableCalculatedValue) ruleVariable;
                createCalculatedValueVariable( valueMap, ruleVariableCalculatedValue );
            }
            else
            {
                throw new IllegalArgumentException( "Unsupported RuleVariable type: " +
                    ruleVariable.getClass() );
            }
        }
    }

    private void createAttributeVariableValue(
        @Nonnull Map<String, RuleVariableValue> valueMap,
        @Nonnull RuleVariableAttribute variable )
    {
        if ( ruleEnrollment == null )
        {
            return;
        }

        String currentDate = dateFormat.format( new Date() );

        RuleVariableValue variableValue;

        if ( currentEnrollmentValues.containsKey( variable.trackedEntityAttribute() ) )
        {
            RuleAttributeValue value = currentEnrollmentValues
                .get( variable.trackedEntityAttribute() );
            variableValue = create( value.value(), variable.trackedEntityAttributeType(),
                Arrays.asList( value.value() ), currentDate );
        }
        else
        {
            variableValue = create( variable.trackedEntityAttributeType() );
        }

        valueMap.put( variable.name(), variableValue );
    }

    private void createCurrentEventVariableValue(
        @Nonnull Map<String, RuleVariableValue> valueMap,
        @Nonnull RuleVariableCurrentEvent variable )
    {
        if ( ruleEvent == null )
        {
            return;
        }

        RuleVariableValue variableValue;

        if ( currentEventValues.containsKey( variable.dataElement() ) )
        {
            RuleDataValue value = currentEventValues.get( variable.dataElement() );
            variableValue = create( value.value(), variable.dataElementType(),
                Arrays.asList( value.value() ), getLastUpdateDate( Arrays.asList( value ) ) );
        }
        else
        {
            variableValue = create( variable.dataElementType() );
        }

        valueMap.put( variable.name(), variableValue );
    }

    private void createPreviousEventVariableValue(
        @Nonnull Map<String, RuleVariableValue> valueMap,
        @Nonnull RuleVariablePreviousEvent variable )
    {
        if ( ruleEvent == null )
        {
            return;
        }

        RuleVariableValue variableValue = null;
        List<RuleDataValue> ruleDataValues = allEventsValues.get( variable.dataElement() );
        if ( ruleDataValues != null && !ruleDataValues.isEmpty() )
        {
            for ( RuleDataValue ruleDataValue : ruleDataValues )
            {
                // We found preceding value to the current currentEventValues,
                // which is assumed to be best candidate.
                if ( ruleEvent.eventDate().compareTo( ruleDataValue.eventDate() ) > 0 )
                {
                    variableValue = create( ruleDataValue.value(), variable.dataElementType(),
                        Utils.values( ruleDataValues ), getLastUpdateDateForPrevious( ruleDataValues ) );
                    break;
                }
            }
        }

        if ( variableValue == null )
        {
            variableValue = create( variable.dataElementType() );
        }

        valueMap.put( variable.name(), variableValue );
    }

    private void createNewestEventVariableValue(
        @Nonnull Map<String, RuleVariableValue> valueMap,
        @Nonnull RuleVariableNewestEvent variable )
    {

        List<RuleDataValue> ruleDataValues = allEventsValues.get( variable.dataElement() );

        if ( ruleDataValues == null || ruleDataValues.isEmpty() )
        {
            valueMap.put( variable.name(), create( variable.dataElementType() ) );
        }
        else
        {
            valueMap.put( variable.name(), create( ruleDataValues.get( 0 ).value(),
                variable.dataElementType(), Utils.values( ruleDataValues ), getLastUpdateDate( ruleDataValues ) ) );
        }
    }

    private String getLastUpdateDate( List<RuleDataValue> ruleDataValues )
    {
        List<Date> dates = new ArrayList<>();
        for ( RuleDataValue date : ruleDataValues )
        {
            Date d = date.eventDate();
            if ( !d.after( new Date() ) )
            {
                dates.add( d );
            }
        }
        return dateFormat.format( Collections.max( dates ) );
    }

    private String getLastUpdateDateForPrevious( List<RuleDataValue> ruleDataValues )
    {
        List<Date> dates = new ArrayList<>();
        for ( RuleDataValue date : ruleDataValues )
        {
            Date d = date.eventDate();
            if ( !d.after( new Date() ) && d.before( ruleEvent.eventDate() ) )
            {
                dates.add( d );
            }
        }
        return dateFormat.format( Collections.max( dates ) );
    }

    private void createNewestStageEventVariableValue(
        @Nonnull Map<String, RuleVariableValue> valueMap,
        @Nonnull RuleVariableNewestStageEvent variable )
    {

        List<RuleDataValue> stageRuleDataValues = new ArrayList<>();
        List<RuleDataValue> sourceRuleDataValues = allEventsValues.get( variable.dataElement() );
        if ( sourceRuleDataValues != null && !sourceRuleDataValues.isEmpty() )
        {

            // filter data values based on program stage
            for ( int i = 0; i < sourceRuleDataValues.size(); i++ )
            {
                RuleDataValue ruleDataValue = sourceRuleDataValues.get( i );
                if ( variable.programStage().equals( ruleDataValue.programStage() ) )
                {
                    stageRuleDataValues.add( ruleDataValue );
                }
            }
        }

        if ( stageRuleDataValues.isEmpty() )
        {
            valueMap.put( variable.name(), create( variable.dataElementType() ) );
        }
        else
        {
            valueMap.put( variable.name(), create( stageRuleDataValues.get( 0 ).value(),
                variable.dataElementType(), Utils.values( stageRuleDataValues ),
                getLastUpdateDate( stageRuleDataValues ) ) );
        }
    }

    private void createCalculatedValueVariable(
        @Nonnull Map<String, RuleVariableValue> valueMap,
        @Nonnull RuleVariableCalculatedValue variable )
    {
        if ( ruleEnrollment == null )
        {
            return;
        }

        RuleVariableValue variableValue;
        if ( calculatedValueMap.containsKey( ruleEnrollment.enrollment() ) )
        {
            if ( calculatedValueMap.get( ruleEnrollment.enrollment() ).containsKey( variable.name() ) )
            {
                String value = calculatedValueMap.get( ruleEnrollment.enrollment() ).get( variable.name() );

                variableValue = create( value, variable.calculatedValueType(),
                    Arrays.asList( value ), dateFormat.format( new Date() ) );
            }
            else
            {
                variableValue = create( variable.calculatedValueType() );
            }
        }
        else
        {
            variableValue = create( variable.calculatedValueType() );
        }

        valueMap.put( variable.name(), variableValue );
    }
}
