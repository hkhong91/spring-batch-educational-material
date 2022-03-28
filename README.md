Spring Batch
===========

To run the application, you need to install DBs.

<pre>
docker-compose -f up -d
</pre>

Connect the mysql and create a schema.

<pre>
create schema `batch`;
create schema `demo`;
</pre>

## Jobs

### createArticleJob
> Read CSV file and save data in DB table.</br>
> You can test by putting a CSV file in src/main/resources and writing the file name as JobParameter.

### createBoardJob
> Reads multiple CSV files and saves data in DB.</br>
> This is a sample processing of partitioning using a local thread, and it needs to be applied to the appropriate business logic.</br>
> You can change the hard-coded path and copy multiple Boards.csv files in src/main/resources and put them in the path to test.

### createOddBoardJob
> After reading and processing DB table data, it stores the data in another table.

### softDeleteBoardJob
> After reading the data to be deleted from the DB table, change the delete flag and save it.

### hardDeleteBoardJob
> After reading the target data to be deleted from the DB table, the data is saved in the backup table.</br>
> Delete the data after saving it to the backup table.
