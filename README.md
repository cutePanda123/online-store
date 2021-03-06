# online-store-backend
Build a highly available, highly concurrent, and distributed online store backend.

#To Do:
1. Change increasing sales operation from sync to async.
2. Refactor the promotion event access token generation logic: move the input validation part into a separated function to follow the single responsibility principle.
3. Set expiration time to all the Redis cached keys.
4. Promotion event token request threshold control on each account or client IP address.
5. Improve error messages to give users more actionable error messages.
6. Stabilize the distributed saved sessions in a Redis cluster.
7. Extend a session expiration time after an incoming request.
8. Change HTTP to HTTPs.
9. Enable SameSite cookie to protect CSRF.
10. Extend guest user session expiration time by periodically sending a keepaliverequest from the client.
11. Implement an SSO service for cross-sites requests.
12. Redis and DB optimization for querying non-existing items to avoid cache fault.
13. Optimize MySQL performance: 
   (1) max_connection = 1000 
   (2) innodb_file_per_table = 1 
   (3) innodb_buffer_pool_size = 1G 
   (4) innodb_log_file_size = 256M 
   (5) innodb_log_buffer_size = 16M 
   (6) innodb_flush_log_at_trx_commit = 2 
   (7) innodb_data_file_path = ibdata1:1G;ibdata2:1G;ibdata3:1G:autoextend 
   (8) master-slave mode configuration 
   (9) data partition: multi-tier routing for new added partitions 
   (10) order database operation: 
      a. BASE (Base Available + Soft state + Eventual consistency): Raft protocol 
      b. Strong Consistency: master-slave mode semi-sync to achieve strong consistency
