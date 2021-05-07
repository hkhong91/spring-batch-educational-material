Spring Batch
===========
MySQL 정보 변경하고 실행 가능합니다.

### createArticleJob
> CSV 파일을 읽어서 DB 테이블에 데이터를 저장합니다.
> src/main/resources 내에 CSV 파일을 넣고 JobParameter로 해당 파일명을 기입하고 테스트 가능합니다.

### createBoardJob
> 여러 개의 CSV 파일을 읽어서 DB에 데이터를 저장합니다.
> 로컬 쓰레드를 이용한 파티셔닝 처리 샘플이며, 적절한 비즈니스 로직에 적용이 필요합니다.
> 하드코딩된 경로 변경하고 src/main/resources 내에 Boards.csv 파일을 여러 개 복사하여 해당 경로에 넣고 테스트 가능합니다.

### createOddBoardJob
> DB 테이블 데이터를 읽어서 처리한 후 다른 테이블에 데이터를 저장합니다.

### softDeleteBoardJob
> DB 테이블 삭제 대상 데이터를 읽은 후, 삭제 플래그를 변경하고 저장합니다.

### hardDeleteBoardJob
> DB 테이블 삭제 대상 데이터를 읽은 후, 백업 테이블에 데이터를 저장합니다.
> 백업 테이블에 데이터를 저장한 후에 데이터를 삭제합니다.