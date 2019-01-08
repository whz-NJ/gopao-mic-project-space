package com.gupaoedu.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import org.apache.kafka.common.utils.ByteUtils;
import java.nio.ByteBuffer;
/**
 * 腾讯课堂搜索 咕泡学院
 * 加群获取视频：608583947
 * 风骚的Michael 老师
 */
public class KafkaConsumerDemo extends Thread{

    private final KafkaConsumer kafkaConsumer;

    public KafkaConsumerDemo(String topic) {
//        byte b;
//        b = 0x3;
//        int value = 4;
//        value |= (b & 127) << 3;
//        int value2 = 4;
//        value2 = value2 | ((b & 127) << 3);
        ByteBuffer bb = ByteBuffer.allocate(20);
        Byte b = (byte)0xa8;
        bb.put(b);
        b = 0x09;
        bb.put(b);
        bb.flip();
        int size = ByteUtils.readVarint(bb);

        ByteUtils.writeVarint(300,bb);
        bb.flip();
        int size2 = ByteUtils.readVarint(bb);
        Properties properties=new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "127.0.0.1:9092");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG,"KafkaConsumerDemo2");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,"false");
        properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG,"1000");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.IntegerDeserializer");
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");
        kafkaConsumer=new KafkaConsumer(properties);
        kafkaConsumer.subscribe(Collections.singletonList(topic));
       /* TopicPartition topicPartition=new TopicPartition(topic,0);
        kafkaConsumer.assign(Arrays.asList(topicPartition));*/
    }

    @Override
    public void run() {
        while(true){
            ConsumerRecords<Integer,String> consumerRecord=kafkaConsumer.poll(1000);
            for(ConsumerRecord record:consumerRecord){
                System.out.println(record.partition()+"->"+"message id:" + record.key() + ",message value receive:"+record.value());
                kafkaConsumer.commitAsync();
            }
        }
    }

    public static void main(String[] args) {
        new KafkaConsumerDemo("test").start();
    }
}
