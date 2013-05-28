package org.ansj.lucene;


import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;
import java.util.Set;

import love.cq.domain.Forest;
import love.cq.library.Library;
import love.cq.util.IOUtil;
import love.cq.util.StringUtil;

import org.ansj.util.MyStaticValue;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;


public class AnsjAnalyzer extends Analyzer {
  Set<String> filter;
  
  public AnsjAnalyzer() {
    
  }

  public AnsjAnalyzer(Set<String> filter) {
    this.filter = filter;
  }

  public AnsjAnalyzer(Settings settings) {
    ESLogger logger = Loggers.getLogger("ansj-analyzer");
    Environment environment = new Environment(settings);

    File userDic = new File(environment.configFile(), MyStaticValue.userDefinePath);
    try {

      long start = System.currentTimeMillis();
      Forest forest = new Forest();

      // 如果系统设置了用户词典.那么..呵呵
      // 加载用户自定义词典
      if (userDic.isFile()) {
        String temp = userDic.getAbsolutePath();
        BufferedReader br = IOUtil.getReader(temp, "UTF-8");
        while ((temp = br.readLine()) != null) {
          if (StringUtil.isBlank(temp)) {
            continue;
          } else {
            Library.insertWord(forest, temp);
          }
        }
      } else {

        System.err.println("用户自定义词典:" + MyStaticValue.userDefinePath + ", 没有这个文件!");
      }
      logger.info("[Dict Loading] {},UserDict Time:{}", userDic.toString(),
          (System.currentTimeMillis() - start));
    } catch (Exception e) {
      logger.info("[Dict Loading] {},Load error!", userDic.toString());
    }
  }

  @Override
  public TokenStreamComponents createComponents(String fieldName, Reader reader) {
    Tokenizer tokenizer = new AnsjTokenizer(reader, filter);
    return new TokenStreamComponents(tokenizer);
  }
}
