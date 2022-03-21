Spring Batch
===========

### createArticleJob
> Read CSV file and save data in DB table.
> You can test by putting a CSV file in src/main/resources and writing the file name as JobParameter.

### createBoardJob
> Reads multiple CSV files and saves data in DB.
> This is a sample processing of partitioning using a local thread, and it needs to be applied to the appropriate business logic.
> You can change the hard-coded path and copy multiple Boards.csv files in src/main/resources and put them in the path to test.

### createOddBoardJob
> After reading and processing DB table data, it stores the data in another table.

### softDeleteBoardJob
> After reading the data to be deleted from the DB table, change the delete flag and save it.

### hardDeleteBoardJob
> After reading the target data to be deleted from the DB table, the data is saved in the backup table.
> Delete the data after saving it to the backup table.
