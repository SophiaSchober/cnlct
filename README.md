# CNL-CT System

The CNL-CT System provides a GUI for specifying pairwise, 3-way and 4-way Combinatorial Testing & Event Sequence Testing 
scenarios in a simple Controlled Natural Language (CNL). After translating such a CNL-specification into an 
ASP program and greedily solving the resulting program (in a row-by-row approach), test cases that a proper 
Covering Array of the desired strength for the described testing scenario are displayed in table-form.


## Running the Application

**Prerequisites**: Maven (tested with version 3.8.2), Java version >= 20.

In order to start the application simply execute the command `mvn clean javafx:run` from the "cnlct"-directory.


## GUI Manual

### Configuring Test Type & Test Strength

Clicking the "Test-Configuration"-Menu-item in the top menu-bar opens up a radio-button menu that enables switching 
between input-parameter/configuration testing and event sequence testing as well as switching between pairwise and 
3-way testing.

### Specifying Testing Scenarios

Test scenario specifications are written into the text area which has the title "Test Specification" and is located in 
left part of the split main menu. Here, sentences adhering to the provided CNL can be entered to describe a custom 
testing scenario. Supported sentence patterns (for the currently selected test type) can be displayed by clicking the 
"Sentence Patterns"-tab next to the "Errors" tab in tab-pane located at the bottom of the application window. Names for parameters, parameter values and events must start with a letter and consist of letters from the English alphabet and digits. Multiple 
test specifications can be loaded at the same time by opening multiple tabs in the upper tab-pane, but only the currently 
selected specification tab is displayed and can have test generation started.

### Supported Sentence Patterns

#### Input Parameter / Configuration Testing

| Nr.                | Pattern Name | Sentence Pattern                                                                                                           | Example                                             |
|-----|--------|----------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------|
| 1. | Parameter Definition | \<parameter> has values \<value-1> {, \<value-i>}* and \<value-n>.                                                         | OS has values Windows, macOS and Linux.             |
| 2. | Specification of (Partial) Seed Tests | Ensure that \<parameter-1>=\<value-1> and \<parameter-2>=\<value-2> {and \<parameter-n>=\<value-n>}*[ is tested].          | Ensure that OS=Windows and Browser=Edge is tested.  |
| 3. | Exclusion of Combinations | Exclude that \<parameter-1>=\<value-1> and \<parameter-2>=\<value-2> {and \<parameter-n>=\<value-n>}*.                     | Exclude that OS=Linux and Browser=Edge. |
| 4. | Conditional Requirements | If \<parameter-1>=\<value-1> {and \<parameter-i>=\<value-i>}* then \<parameter-n>=\<value-n> {and \<parameter-n+j>=\<value-n+j>}*. | If Browser=Safari then OS=macOs. |

#### Event Sequence Testing

| Nr. | Pattern Name          | Sentence Pattern | Example |
|---|-----------------------|----|----|
| 1. | Event Definition      | \<event-1> {, \<event-i>}*[ and \<event-j>] are events. | AddMessage, AddAttachment, AddSubject, EditMessage, EditSubject and AddRecipient are events. | 
| 2. | Exclusion of Subsequences | Exclude that \<event-1> happens before \<event-2>. | Exclude that EditMessage happens before AddMessage. |
| 3. | Fixation of Subsequences | [Ensure that ]\<event-1> happens before \<event-2>. | Ensure that AddSubject happens before EditSubject. | 
| 4. | Seed Test Definition  | Ensure that (\<event-1>, \<event-2>, ..., \<event-n>) is tested. | Ensure that (AddRecipient, AddSubject, AddMessage, EditMessage, EditSubject) is tested. |

### Generating Test-Cases
Test-case generation can be started for the currently selected (and displayed) test specification by clicking the 
"Generate" Button. If errors occur during translating the specification (e.g. due to unknown sentence patterns), they 
are displayed in the "Errors"-tab which is located next to the "Sentence Patterns"-tab in the tab-pane at the bottom 
of the application window. If the translation was successful and a set of test cases that correspond to a covering 
array (modulo the potential excluded combinations contained in the given specification) of the desired strength is 
displayed as a table in the right part of the main menu with the title "Generated Test Cases".


### Saving/Opening Specifications & Exporting Generated Test-Cases
Clicking the menu-item "File" in the menu-bar at the top of the application window opens up a menu which provides options 
for saving newly created or modified specifications, opening up existing specifications and exporting generated test 
cases. Test specifications are saved as text-files (*.txt) and test cases are exported as comma-separated-value files 
(*.csv).
