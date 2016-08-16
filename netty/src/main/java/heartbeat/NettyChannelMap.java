package heartbeat;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 *
 * @ClassName: NettyChannelMap
 * @Description: TODO
 * @author yao.b
 * @date 2015年9月25日 上午11:37:35
 *
 */
public class NettyChannelMap {
    private static Map<String,Channel> map= new ConcurrentHashMap<>();
    public static void add(String clientId,Channel socketChannel){
        map.put(clientId,socketChannel);
    }
    public static Channel get(String clientId){
        Channel channel = map.get(clientId);
        return channel;

    }

    public static Map<String,Channel> getAllMap(){
        return map;
    }

    public static void remove(Channel channel){
        for (Map.Entry entry:map.entrySet()){
            if (entry.getValue()==channel){
                map.remove(entry.getKey());
            }
        }
    }
}