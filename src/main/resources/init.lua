internal_handlers = {}
_G.current_loading_file = nil

events = {}
setmetatable(events, {
	__newindex = function(_, eventName, func)
		if type(func) ~= "function" then return end

		if not internal_handlers[eventName] then
			internal_handlers[eventName] = {}
		end

		local entry = {
			fn = func,
			source = _G.current_loading_file
		}
		table.insert(internal_handlers[eventName], entry)
	end,
	__index = function(_, eventName)
		return internal_handlers[eventName]
	end
})

function tableToString(tbl, indent)
	if not indent then indent = 0 end
	local formatting = string.rep("  ", indent)

	if type(tbl) ~= "table" then
		return tostring(tbl)
	end

	local result = "{\n"
	for k, v in pairs(tbl) do
		local key = k
		if type(k) == "string" then key = '"'..k..'"' end

		if type(v) == "table" then
			result = result .. formatting .. "  [" .. tostring(key) .. "] = " .. tableToString(v, indent + 1) .. ",\n"
		elseif type(v) == "string" then
			result = result .. formatting .. "  [" .. tostring(key) .. "] = " .. '"' .. v .. '"' .. ",\n"
		else
			result = result .. formatting .. "  [" .. tostring(key) .. "] = " .. tostring(v) .. ",\n"
		end
	end
	return result .. formatting .. "}"
end

logger:info("[Lua] Loaded init file.")