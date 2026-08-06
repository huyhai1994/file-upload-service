local current = redis.call('INCR', KEYS[1])

if current == 1 then
	redis.call('PEXPIRE', KEYS[1], ARGV[1])
end

return current