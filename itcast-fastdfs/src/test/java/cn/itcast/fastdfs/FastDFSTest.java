package cn.itcast.fastdfs;

import org.csource.fastdfs.*;
import org.junit.Test;

import java.io.IOException;

public class FastDFSTest {

    @Test
    public void test() throws Exception {
        //配置文件地址（追踪服务器ip和端口号）
        String confFilename = ClassLoader.getSystemResource("fastdfs/tracker.conf").getPath();
        //- 设置全局配置（追踪服务器地址）
        ClientGlobal.init(confFilename);
        //- 创建追踪服务器客户端对象
        TrackerClient trackerClient = new TrackerClient();
        //- 获取追踪服务器服务对象
        TrackerServer trackerServer = trackerClient.getConnection();
        //- 创建存储服务器对象；通过追踪服务器可以获取到相关信息
        StorageServer storageServer = null;
        //- 利用上述两个服务对象构建存储客户端对象
        StorageClient storageClient = new StorageClient(trackerServer, storageServer);
        //- 上传文图并处理返回结果
        /**
         * 参数1：文件路径
         * 参数2：文件扩展名（后缀jpg）
         * 参数3：文件信息
         * 返回的数组数据：
         * group1 组名
         * M00/00/00/wKgMqF1YyfiAb5fYAABw0se6LsY26.jpg 文件相对路径
         */
        String[] upload_file = storageClient.upload_file("D:\\itcast\\pics\\575968fcN2faf4aa4.jpg", "jpg", null);
        if (upload_file != null && upload_file.length > 0) {
            for (String str : upload_file) {
                System.out.println(str);
            }

            String groupName = upload_file[0];
            String filename = upload_file[1];
            //获取存储服务器的ip
            ServerInfo[] serverInfos = trackerClient.getFetchStorages(trackerServer, groupName, filename);
            for (ServerInfo serverInfo : serverInfos) {
                System.out.println("存储服务器的ip = " + serverInfo.getIpAddr() + "；端口号为：" + serverInfo.getPort());
            }

            //图片可以访问的地址
            String url = "http://" + serverInfos[0].getIpAddr() + "/" + groupName + "/" + filename;
            System.out.println(url);
        }

    }
}
