# online-store-backend

Build a high available, high concurrent, and distributed online store backend.
#To Do:
1. Change Increasing Sales operation from sync to async
2. Refactor the promotion event access token generation logic: move the input validation part into a
   separated function to follow the single responsibility principle.
3. Set expire time to all the Redis cached keys
4. Promotion event token request threshold control on each account or client IP address.
