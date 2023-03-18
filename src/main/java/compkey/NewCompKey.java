package compkey;

import java.io.FileReader;
import java.util.*;

import compkey.midKeyThread;

public class NewCompKey {
    public static List<Map.Entry<String,Double>> compkey(String seedKey, int midNum) throws Exception{
        System.out.println("查询种子关键词的相关搜索记录...");
        //从清洗过的数据中提取出与种子关键字相关的搜索信息并保存
        int seedInfoCounter = util.search(seedKey,"src/main/resources/compkeyFiles/seedSearchResult.txt");

        System.out.println("========================================================");
        System.out.println("开始查找中介关键词...");
        Map<String,Integer> midCountMap =midKeyThread.cut_count(9,seedInfoCounter);
        List<String> midKey = new ArrayList<>();
        //挑选中介词
        int flag = 0;
        for(String key : midCountMap.keySet()){
            if(!key.equals(seedKey)&&!key.contains(seedKey)&&flag<midNum){
                midKey.add(key);
                flag++;
            }else {
                continue;
            }
            if(flag>=midNum){break;}
        }
        System.out.println("确定中介关键词为："+midKey);

        System.out.println("========================================================");
        System.out.println("开始查找竞争性关键词...");
        Map<String,String> mid_comp = new HashMap<>();
        Map<String,Integer> mid_comp_ka = new HashMap<>();//midKey-ka值
        for(int i=0;i<midKey.size();i++){
            String midkey = midKey.get(i);
            int midInfoCounter = util.compSearch(seedKey,midkey,"src/main/resources/compkeyFiles/midSearchResult.txt");
            //定义当前中介词的分词词频情况并挑选竞争关键词,同时完成ka搜索量的确认
            Map<String,Integer> compCountMap =compKeyThread.cut_count(9,midInfoCounter);
            //挑选竞争词
            int compflag = 0;
            for(String key : compCountMap.keySet()){
                if(!key.equals(midkey)&&!key.equals(seedKey)&&!key.contains(seedKey)&&compflag<1&&!mid_comp.containsValue(key)){
                    mid_comp.put(midkey,key);
                    mid_comp_ka.put(midkey,compCountMap.get(key));
                    compflag++;
                }else {
                    continue;
                }
                if(flag>=1){break;}
            }
        }
        System.out.println("确定每个中介对应的竞争关键词为："+mid_comp);


        System.out.println("========================================================");
        System.out.println("开始计算竞争度...");
        //确定相关搜索量
        //种子搜索量
        int s = midCountMap.get(seedKey);
        //种子和中介关键词一起出现的搜索量sa
        Map<String,Integer> seed_mid_sa = new HashMap<>();//midKey-sa
        //定义中介关键词的搜索量a
        Map<String,Integer> mid_a = new HashMap<>();//midKey-a
        //计算权重weight=sa/s
        Map<String,Double> mid_weight = new HashMap<>();//midKey-weight
        //定义竞争关键词的竞争度
        Map<String,Double> comp = new TreeMap<>();
        for (int i =0;i<midKey.size();i++){
            //确定此次循环下的中介关键词
            String midkey = midKey.get(i);

            //sa
            try(Scanner sc = new Scanner(new FileReader("src/main/resources/compkeyFiles/seedSearchResult.txt"))) {
                int count = 0;
                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    if (line.contains(seedKey)&&line.contains(midkey)){
                        count++;
                    }
                }
                seed_mid_sa.put(midkey,count);
                //计算权重
                double weight = (double)count/(double)s;
                mid_weight.put(midkey,weight);
            }catch (Exception e){
                e.printStackTrace();
            }

            //a
            try(Scanner sc = new Scanner(new FileReader("src/main/resources/compkeyFiles/cleanResult.txt"))) {
                int count = 0;
                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    if (line.contains(midkey)){
                        count++;
                    }
                }
                mid_a.put(midkey,count);
            }catch (Exception e){
                e.printStackTrace();
            }

            //最终竞争度计算
            double ans;
            if((double)mid_a.get(midkey)-(double) seed_mid_sa.get(midkey)==0){
                ans = -1;
            }else {
                ans = (double) mid_comp_ka.get(midkey)/(double) (mid_a.get(midkey)-seed_mid_sa.get(midkey));
            }
            double value = mid_weight.get(midkey)*ans;
            comp.put(mid_comp.get(midkey),value);
        }
        System.out.println("ka:"+mid_comp_ka);
        System.out.println("a:"+mid_a);
        System.out.println("sa:"+seed_mid_sa);
        System.out.println("s:"+s);
        System.out.println("weight:"+mid_weight);

        //排序输出
        comp = util.sortComp(comp);
        System.out.println("“" + seedKey + "”的竞争性关键词关于竞争度排序如下:");
        List<Map.Entry<String,Double>> result = new ArrayList<>(comp.entrySet());
        System.out.println(result);
        System.out.println("CompKey算法结束...");
        return result;
    }

    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();    //获取开始时间
        compkey("图片",3);
        long time1 = System.currentTimeMillis();    //获取结束时间
        System.out.println("程序运行时间：" + (time1 - startTime) + "ms");    //输出程序运行时间
    }
}
