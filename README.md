# CIproject

java.reflect


    /**
     * 获得存取KV值的类实例
     *
     * @return
     */
    public static KVAccess getKVAccess() {
        KVAccess access = null;
        String className = SystemParam.getParamByKey("kv_class");
        try {
            Class c = Class.forName(className);  //获取类名
            access = (KVAccess) c.newInstance(); //new一个新的实例
        } catch (ClassNotFoundException e) {
            logger.error("Class [" + LogEncoder.encodeObject(className) + "] not found.");
        } catch (InstantiationException e) {
            logger.error("Class [" + LogEncoder.encodeObject(className) + "] instantiation failed.");
        } catch (IllegalAccessException e) {
            logger.error("Class [" + LogEncoder.encodeObject(className) + "] illegal access.");
        }
        return access;
    }
    
    
    
    public class RedisKVAccess implements KVAccess, Serializable {
    private static final Logger logger = Logger.getLogger(RedisKVAccess.class);
    private ShardedJedis jedis = null;
    private static final Object lock = new Object();

    /**
     * 获得当前的jedis连接，为空则初始化
     *
     * @return
     */
    protected ShardedJedis getJedis() {
        try {
            synchronized (lock) {
                if (null == jedis) {
                    jedis = JedisClient.getInstance().getJedis();
                    logger.info("Succeed to get a jedis connection.");
                }
            }
        } catch (JedisException je) {
            logger.error("Could not get a resource from the redis pool, "
                    + ExceptionUtils.getExceptionMessage(je));
        }
        return jedis;
    }

    /**
     * 关闭连接
     */
    public void close() {
        synchronized (lock) {
            if (null != jedis) {
                JedisClient.getInstance().closeJedis(jedis);
                logger.info("Succeed to close the jedis connection.");
            }
        }
    }

    @Override
    public Object exists(String key) {
        boolean result = false;
        try {
            if (null == jedis) {
                getJedis();
            }
            if (null != jedis) {
                result = jedis.exists(key);
                logger.info("Check existence of key [" + key + "].");
            }
        } catch (JedisException je) {
            logger.error("Could not get a resource from the redis pool, "
                    + ExceptionUtils.getExceptionMessage(je));
        }
        return result;
    }

    /**
     * 存储数据，如果成功返回OK
     *
     * @param key
     * @param value
     * @return
     */
    public Object set(String key, String value) {
        String result = null;
        try {
            if (null == jedis) {
                getJedis();
            }
            if (null != jedis) {
                result = jedis.set(key, value);
                logger.info("Set value for key [" + key + "] result: [" + result + "].");
            }
        } catch (JedisException je) {
            logger.error("Could not get a resource from the redis pool, "
                    + ExceptionUtils.getExceptionMessage(je));
        }
        return result;
    }

    @Override
    public Object setex(String key, int ttl, String value) {
        String result = null;
        try {
            if (null == jedis) {
                getJedis();
            }
            if (null != jedis) {
                result = jedis.setex(key, ttl, value);
                logger.info("Set value for key: [" + key + "] with ttl: [" + ttl + "] result: [" + result + "].");
            }
        } catch (JedisException je) {
            logger.error("Could not get a resource from the redis pool, "
                    + ExceptionUtils.getExceptionMessage(je));
        }
        return result;
    }

    /**
     * 如果该key已存在，则不写入并返回0；如果不存在，则写入并返回1
     *
     * @param key
     * @param value
     * @return
     */
    public Object setnx(String key, String value) {
        Long result = 0L;
        try {
            if (null == jedis) {
                getJedis();
            }
            if (null != jedis) {
                result = jedis.setnx(key, value);
                logger.info("Setnx value for key [" + key + "] result: [" + result + "].");
            }
        } catch (JedisException je) {
            logger.error("Could not get a resource from the redis pool, "
                    + ExceptionUtils.getExceptionMessage(je));
        }
        return result;
    }

    /**
     * 如果集合中已存在value，则返回0，否则写入并返回1
     *
     * @param key
     * @param value
     * @return
     */
    public Object sadd(String key, String value) {
        Long result = 0L;
        try {
            if (null == jedis) {
                getJedis();
            }
            if (null != jedis) {
                result = jedis.sadd(key, value);
                logger.info("Add set element for key [" + key + "] result: [" + result + "].");
            }
        } catch (JedisException je) {
            logger.error("Could not get a resource from the redis pool, "
                    + ExceptionUtils.getExceptionMessage(je));
        }
        return result;
    }

    /**
     * 查询key
     *
     * @param key
     * @return
     */
    public Object get(String key) {
        String value = null;
        try {
            if (null == jedis) {
                getJedis();
            }
            if (null != jedis) {
                value = jedis.get(key);
                if (null == value) {
                    logger.warn("The key [" + key + "] does not exists in redis.");
                } else {
                    logger.info("Get value of [" + key + "].");
                }
            }
        } catch (JedisException je) {
            logger.error("Could not get a resource from the redis pool, "
                    + ExceptionUtils.getExceptionMessage(je));
        }
        return value;
    }

    /**
     * 查询key存储的set集合
     *
     * @param key
     * @return
     */
    public Object smembers(String key) {
        Set<String> value = null;
        try {
            if (null == jedis) {
                getJedis();
            }
            if (null != jedis) {
                value = jedis.smembers(key);
                if (null == value || value.size() == 0) {
                    logger.warn("The key [" + key + "] does not exists in redis.");
                } else {
                    logger.info("Get members of [" + key + "].");
                }
            }
        } catch (JedisException je) {
            logger.error("Could not get a resource from the redis pool, "
                    + ExceptionUtils.getExceptionMessage(je));
        }
        return value;
    }

    /**
     * 删除key
     *
     * @param key
     * @return
     */
    public Object del(String key) {
        Long result = 0L;
        try {
            if (null == jedis) {
                getJedis();
            }
            if (null != jedis) {
                result = jedis.del(key);
                logger.info("Delete key [" + key + "] result: [" + result + "].");
            }
        } catch (JedisException je) {
            logger.error("Could not get a resource from the redis pool, "
                    + ExceptionUtils.getExceptionMessage(je));
        }
        return result;
    }
}
