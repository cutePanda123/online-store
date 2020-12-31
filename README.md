# online-store-backend

Build a high available, high concurrent, and distributed online store backend.
#To Do:
1. Change Increasing Sales operation from sync to async
2. Refactor the promotion event access token generation logic: move the input validation part into a
   separated function to follow the single responsibility principle.
3. Set expire time to all the Redis cached keys
4. Promotion event token request threshold control on each account or client IP address.
5. Improve error message to give user more actionable error messages
6. Stabilize the distributed saved sessions in a Redis cluster.
7. Extend a session expiration time after an incoming request.
8. Change HTTP to HTTPs.
9. Enable SameSite cookie to protect CSRF.
10. Extend guest user session expiration time by periodically sending a keepalive
request from the client.
11. Implement a SSO service for cross-sites requests.
12. Redis and DB optimization for querying non-existing items to avoid cache fault.