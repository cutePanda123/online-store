local args = ngx.req.get_uri_args()
local id = args["id"]
local redis = require "resty.redis"
local cache = redis:new()
local ok,err = cache:connect("10.0.17.6", 6379)
local good_model = cache:get("good_"..id)
if good_model == ngx.null or good_model == nil then
	local resp = ngx.location.capture("/good/get?id="..id)
	good_model = resp.body
end

ngx.say(good_model)
