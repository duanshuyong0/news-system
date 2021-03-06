package com.sunxn.news.webcrawler.pipeline;

import com.sunxn.news.webcrawler.pojo.CarouselNews;
import com.sunxn.news.webcrawler.pojo.NewsDetail;
import com.sunxn.news.webcrawler.pojo.NewsItem;
import com.sunxn.news.webcrawler.service.CarouselNewsService;
import com.sunxn.news.webcrawler.service.NewsDetailService;
import com.sunxn.news.webcrawler.service.NewsItemService;
import com.sunxn.news.webcrawler.service.TextRankKeyword;
import com.sunxn.news.webcrawler.utils.HtmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.htmlparser.util.ParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

/**
 * @description:    新闻轮播图数据集处理管道
 * @data: 2020/4/16 17:02
 * @author: xiaoNan
 */
@Slf4j
@Component
public class NewsCarouselDataPipeline implements Pipeline {

    @Autowired
    private NewsItemService newsItemService;
    @Autowired
    private NewsDetailService newsDetailService;
    @Autowired
    private CarouselNewsService carouselNewsService;
    @Autowired
    private TextRankKeyword textRankKeyword;

    @Override
    @Transactional
    public void process(ResultItems resultItems, Task task) {
        NewsItem newsItem = resultItems.get("newsItem");
        if (newsItem == null) {
            log.info("【爬虫服务】newsItem信息为null");
            return;
        }
        newsItemService.save(newsItem);

        NewsDetail newsDetail = resultItems.get("newsDetail");
        if (newsDetail == null || newsItem.getId() == null) {
            log.info("【爬虫服务】newsDetail信息为null 或者 newsItem.getId为null");
            return;
        }
        newsDetail.setNewsId(newsItem.getId());
        String content = "";
        try {
            content = HtmlUtil.getText(newsDetail.getContent());
        } catch (ParserException e) {
            log.error("【爬虫数据处理管道】 纯文本内容处理失败，异常信息为：", e);
        }
        newsDetail.setKeyword(textRankKeyword.getKeyword(newsItem.getTitle(), content));
        newsDetailService.save(newsDetail);

        CarouselNews carouselNews = resultItems.get("carouselNews");
        if (carouselNews == null) {
            log.info("【爬虫服务】carouselNews信息为null");
            return;
        }
        carouselNews.setCategoryId(newsItem.getCategoryId());
        carouselNews.setNewsId(newsItem.getId());
        carouselNewsService.save(carouselNews);
    }
}
