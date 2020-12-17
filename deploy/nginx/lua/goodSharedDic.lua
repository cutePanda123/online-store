function get_from_cache(key)
	local cache_ngx = ngx.shared.my_cache
	local value = cache_ngx:get(key)
	return value
end

function set_to_cache(key, value, expTime)
	if not expTime then
		expTime = 0
	end
	local cache_ngx = ngx.shared.my_cache
	local succ,err,forcible = cache_ngx:set(key,value,expTime)
	return succ
end

local args = ngx.req.get_uri_args()
local id = args["id"]
local good_model = get_from_cache("good_"..id)
if good_model == nil then
	local resp = ngx.location.capture("/good/get?id="..id)
	good_model = resp.body
	set_to_cache("good_"..id,good_model,1*60)
end
ngx.say(good_model)
