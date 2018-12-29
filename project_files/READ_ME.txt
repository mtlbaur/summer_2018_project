====================================================================================================================================================
FILE FORMAT:

This program requires a custom CSV file with a specific format.
In a single row of the input file should have the following values in the exact same order:

*plate_number*
*plate_type*
*row*
*col*
*reading*
*reading / average_of_left_standards*
*reading / average_of_right_standards*
*atomic_symbol*
*atomic_symbol_position_in_element_ratios*
*element_ratios*
*iron_nonstandard*
*copper_nonstandard*
*data_id*

In practice a few rows in the file should look like this:

101 red 0 0 1.545824824174791 1.2228187919463085 0.5592960196459634 fe 0 [0:0:0:1] 1 0 1500907535
101 red 0 0 1.545824824174791 1.2228187919463085 0.5592960196459634 cr 1 [0:0:0:1] 1 0 1500907535
101 red 0 0 1.545824824174791 1.2228187919463085 0.5592960196459634 sr 2 [0:0:0:1] 1 0 1500907535
101 red 0 0 1.545824824174791 1.2228187919463085 0.5592960196459634 cu 3 [0:0:0:1] 1 0 1500907535
101 red 0 1 3.213822411621355 2.542281879194631 1.1627954568709713 fe 0 [3:4:5:0] 1 0 1500907535
101 red 0 1 3.213822411621355 2.542281879194631 1.1627954568709713 cr 1 [3:4:5:0] 1 0 1500907535
101 red 0 1 3.213822411621355 2.542281879194631 1.1627954568709713 sr 2 [3:4:5:0] 1 0 1500907535
101 red 0 1 3.213822411621355 2.542281879194631 1.1627954568709713 cu 3 [3:4:5:0] 1 0 1500907535

====================================================================================================================================================
GETTING THE INPUT FILE:

Login to cpanel (URL should be something like: solararmy.fsuhosting.com/cpanel).
Once you have logged in, you should see several sections with different labels.
You should be able to see "FILES", "DATABASES", "DOMAINS", etc.
Under the "DATABASES" category, click "phpMyAdmin".

Once phpMyAdmin has finished loading, look at the leftmost side of the screen.
You should be able to see an expandable directory called "solararm".
You can tell that it is expandable by the "+" to the left of the "solararm" name.
Click the "+".

Now you should see several new expandable directories underneath "solararm" such as "solararm_backup_data", "solararm_data", etc.
Click on the name "solararm_data" - DO NOT just click on the "+" next to "solararm_data".

If you correctly click on the name, the solararm_data directory and all its subdirectories should be highlighted in a gray color.
Now that you have selected the solararm_data table, you can run an SQL (Structured Query Language) query on it.
Along the top horizontal edge of the screen, you should see several tabs called "Structure", "SQL", "Search", etc.
Click on the "SQL" tab.

You should now be looking at a large blank text area wherein you can type an SQL query.
Copy the following SQL query and paste it into this text area.

SELECT element_data.plate_no, plate_type, ratio_data.row_index, ratio_data.col_index, result.reading, result.ratio_to_standard1, result.ratio_to_standard2, element_data.atomic_symbol, pos, ratio, iron_nonstandard, copper_nonstandard, data_id
FROM element_data, plate_data, ratio_data, result
WHERE (element_data.plate_no = plate_data.plate_no AND element_data.plate_no = ratio_data.plate_no AND element_data.plate_no = result.plate_no AND ratio_data.row_index = result.row_index AND ratio_data.col_index = result.col_index)
ORDER BY data_id ASC

On the bottom right you should see a button called "Go", click this when you have pasted the SQL query into the text area.
If you get an error, it is very likely you did not copy the query in its entirety.
The query itself should take less than a second to complete.
If it takes longer than 10 seconds, odds are the query was incorrect.

You should now be looking at a table with headers such as "plate_no", "plate_type", "row_index", etc.
Above this table, you should see a yellow-highlighted line that says something like:

"Showing rows 0 - 24 (117900 total, Query took 0.3022 seconds.)"

Your screen should have a number equal or greater to the "117900" part of that line.
If this is not the case, it means some data was deleted from the database OR the query was not correct.
At the bottom of the screen you should see several buttons like "Print", "Copy to clipboard", "Export", etc.
Click on "Export".

You should now see two sections called "Export method" and "Format".
Under "Export method", select "Custom - display all possible options".
Now you should see several more sections.
I will go through each of them and tell you the setting it should be set to.
Some of these settings may already be correct by default.

Under "Format" select "CSV". NOTE: "CSV" stands for "Comma-Separated Values" which is NOT the kind of file we want.
                                   We select the "CSV" option regardless because it enables some settings that we will need.
                                   We will use the space character instead of the comma to separate our columns.

Under "Rows" select "Dump all rows".

Under "Output" unselect "Rename exported databases/tables/columns".
               unselect "Use LOCK TABLES statement".
               select "Save output to a file".
               for "File name template:" write "@TABLE@" and select "use this for future exports".
               for "Character set of the file:" select "utf-8".
               for "Compression:" select "None".
               unselect "View output as text".
               make sure "Skip tables larger than ____ MiB" is left empty.

Under "Format-specific options:" change "Columns separated with:" to " " (a space character).
                                 change "Columns enclosed with:" to "" (no character).
                                 change "Columns escaped with:" to "" (no character).
                                 change "Lines terminated with:" to "AUTO".
                                 change "Replace NULL with:" to "NULL".
                                 unselect "Remove carriage return/line feed characters within columns".
                                 unselect "Put columns names in the first row".

That should be all the settings.
At the bottom of the screen there should be a "Go" button.
Click this and you will be prompted with a standard download-a-file dialog.
Find the directory you want to save the file to (probably the same folder that contains the ".jar" file).
Rename the file if you want. 
It does not matter which file name you choose, but you MUST NOT change the file extension.
All extensions other than ".csv" will not be recognized by the program.
Once you enter the desired file name and find the correct directory, click "Save" and wait for the file to download.
Now you have the custom "CSV" file.

====================================================================================================================================================
RUNNING THE PROGRAM:

The program should be in the form of a ".jar" file - the file extension should be ".jar" - and it should reside in the same folder as the input data file.
Double-click on the ".jar" file.
You should now see a small dialog titled "Plate Data Formatter".
It should have four different options for the user.
I will go through them and explain their effects.

"Input file name:"
   This is the file name of the input data file (the ".csv" file).
   This file MUST be in the same location as the ".jar" file.

"Lowest acceptable rating:"
   This is the lowest acceptable rating a "valid" plate can have.
   This can be a decimal or integer value.
   The rating is determined by finding the highest value divided by the average of either the left or right standard.
   A rating of 1 for a red-lead plate would mean the plate has at least one cell wherein the value of the cell's reading
   is the same as the average of the iron standard. 
   This means that cell's reading is "as good as" the average of the iron standard on that plate.                              

"Highest acceptable coefficient of variation in %:"
   This is the highest acceptable coefficient of variation a "valid" plate can have.
   If you are unfamiliar with the term "coefficient of variation", it - for our purposes - is the same
   as "relative standard deviation".
   This threshold is only used on the "high" standard of a plate.
   A plate will always have two standards where one of them is higher than the other.
   The low standard is more susceptible to high coefficients of variation so it is not checked.
   If the high standard of the plate has a coefficient of variation lower than the threshold, it is considered "valid".

"Show target elements dialog:"
   If this option is selected, a dialog titled "Select Target Elements" will appear during the formatting process.
   This dialog will allow the user to select the "target elements" via a list of buttons with atomic symbols next to them.
   When the user clicks the "Search" button on this dialog, the program will create an additional file called "target_cells.txt".
   This file will contain all plates and all cells that contain the selected elements.

When you are finished setting the above options, you can run the program by clicking the "Format" button at the bottom of the dialog.
NOTE: if you run the program a second time, all the output text files in the programs folder will be OVERRIDDEN. 
If you do not want this to happen, move the output text files to a different folder WITHOUT the program executable file.
The "Clear Fields" button will empty the fields of the first three options and unselect the fourth.
The "Instructions" button will display the contents of a text file called "READ_ME.txt" in a dialog.
This text file contains instructions on how to use this program.

====================================================================================================================================================