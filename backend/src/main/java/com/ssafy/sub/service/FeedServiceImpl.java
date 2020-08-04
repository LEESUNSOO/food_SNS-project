package com.ssafy.sub.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.ssafy.sub.dto.Feed;
import com.ssafy.sub.dto.FeedHashtag;
import com.ssafy.sub.dto.FeedHashtagKey;
import com.ssafy.sub.dto.Hashtag;
import com.ssafy.sub.exception.RestException;
import com.ssafy.sub.model.response.ResponseMessage;
import com.ssafy.sub.model.response.StatusCode;
import com.ssafy.sub.repo.DBFileRepository;
import com.ssafy.sub.repo.FeedHashtagQueryDsl;
import com.ssafy.sub.repo.FeedHashtagRepository;
import com.ssafy.sub.repo.FeedQueryDsl;
import com.ssafy.sub.repo.FeedRepository;
import com.ssafy.sub.repo.HashtagQueryDsl;
import com.ssafy.sub.repo.HashtagRepository;

@Service
public class FeedServiceImpl implements FeedService {

	@Autowired
	FeedRepository feedRepository;
	@Autowired
	HashtagRepository hashtagRepository;
	@Autowired
	HashtagQueryDsl hashtagQueryDsl;
	@Autowired
	FeedHashtagRepository feedHashtagRepository;
	@Autowired
	FeedHashtagQueryDsl feedHashtagQueryDsl;
	@Autowired
	DBFileRepository dbFileRepository;
	@Autowired
	FeedQueryDsl feedQueryDsl;

	@Override
	public List<Feed> feedHomePageList() {
		//return feedRepository.findAll();
		return feedQueryDsl.findFeedList();
	}
	
	@Override
	public List<Hashtag> findFeedHashtagList(int fid) {
		List<Hashtag> hashtagList = feedHashtagQueryDsl.findHashtagById(fid);
		return hashtagList;
	}
	
	@Override
	public List<Feed> feedUserPageList(int uid) {
		List<Feed> feeds = feedQueryDsl.findFeedListByUid(uid);
		return feeds;
	}

	@Override
	public List<Feed> findAllByFollower(int id) {
		List<Feed> test = feedQueryDsl.findAllByFollower(id);
		
		for (Feed feed : test) {
			System.out.println(feed.toString());
		}
		 
		if(test.size() == 0)
			System.out.println("null z?");
		return test;
	}

	@Override
	public Feed feedDetail(int id) {
		Feed feed = feedRepository.findById(id)
		.orElseThrow(() -> new RestException(StatusCode.NOT_FOUND, ResponseMessage.NOT_FOUND_FEED, HttpStatus.NOT_FOUND));
		
		if(dbFileRepository.findAllByFid(id).isPresent()) {
			feed.setDbFiles(dbFileRepository.findAllByFid(id).get());
		}

		return feed;
	}

	@Override
	public Feed feedInsert(Feed feed) {
		feed.setRegdate(new Date());
		return feedRepository.save(feed);
	}

	@Override
	@Transactional
	public Feed feedUpdate(int id, Feed feed) {
		Date now = new Date();
		Optional<Feed> updateFeed = feedRepository.findById(id);
		if(!updateFeed.isPresent()) throw new RestException(StatusCode.NOT_FOUND, ResponseMessage.NOT_FOUND_FEED, HttpStatus.NOT_FOUND);
		updateFeed.get().setTitle(feed.getTitle());
		updateFeed.get().setContent(feed.getContent());
		updateFeed.get().setEditdate(now);

		return updateFeed.get();
	}

	@Override
	public Long feedDelete(int id) {
		return feedRepository.deleteById(id);
	}

	@Override
	public List<Hashtag> findAllHashtag() {
		return hashtagRepository.findAll();
	}

	@Override
	public boolean findByContent(String content) {
		if(hashtagRepository.findByContent(content)!=null) {
			return false;
		}
		return true;
	}

	@Override
	public Hashtag hashtagInsert(String content) {
		Hashtag h = new Hashtag();
		h.setContent(content);
		Hashtag hashtag = hashtagRepository.save(h);
		if(hashtag==null)
			throw new RestException(StatusCode.BAD_REQUEST, ResponseMessage.FAIL_CREATE_HASHTAG);
		return hashtag;
	}

	@Override
	public Hashtag hashtagUpdate(int hid, String content) {
		Optional<Hashtag> updateHashtag = hashtagRepository.findById(hid);
		if(!updateHashtag.isPresent())
			throw new RestException(StatusCode.NOT_FOUND, ResponseMessage.NOT_FOUND_HASHTAG);
		updateHashtag.get().setContent(content);
		return updateHashtag.get();
	}

	@Override
	public Hashtag hashtagDetail(int hid) {
		return hashtagRepository.findById(hid)
				.orElseThrow(() -> new RestException(StatusCode.NO_CONTENT, ResponseMessage.NOT_FOUND_HASHTAG));
	}
	
	@Override
	public List<Hashtag> feedHashtagList(int fid) {
		List<FeedHashtag> feedHashtagList = new ArrayList<FeedHashtag>();
		List<Hashtag> hashtagList = new ArrayList<Hashtag>();
		
		if(feedHashtagQueryDsl.findAllByFid(fid)!=null) {
			feedHashtagList = feedHashtagQueryDsl.findAllByFid(fid);
		}
		
		Hashtag hashtag;
		for(FeedHashtag fh: feedHashtagList) {
			hashtag = hashtagRepository.findById(fh.getFeedHashtagkey().getFid()).get();
			hashtagList.add(hashtag);
		}
		
		return hashtagList;
	}

	@Override
	public int getFeedCount(int uid) {
		// feed 수
		int feedCount = 0;
		feedCount = feedRepository.findByUid(uid).size();
		return feedCount;
	}

	@Override
	public int feedHashtagListInsert(List<Hashtag> hashtagList) {
		int fid = (int) feedRepository.count();
		System.out.println(fid);
		String content;
		int hid;
		FeedHashtag feedHashtag = new FeedHashtag();
		Hashtag hashtag;
		System.out.println(hashtagList.toString());
		for(Hashtag h: hashtagList) {
			content = h.getContent();
			System.out.println();
			
			if(hashtagRepository.findByContent(content)!=null) {
				hid = hashtagRepository.findByContent(content).getId();
			}else {
				Hashtag ht = new Hashtag();
				ht.setContent(content);
				hashtag = hashtagRepository.save(ht);
				hid = hashtag.getId();
			}
			
			feedHashtag.setFeedHashtagkey(new FeedHashtagKey(fid, hid));
			feedHashtagRepository.save(feedHashtag);
		}
		
		return 0;
	}

	@Override
	public List<Hashtag> feedHashtagListUpdate(int fid, List<Hashtag> hashtagList) {
		String content;
		int hid;
		FeedHashtag feedHashtag = new FeedHashtag();
		Hashtag hashtag;
		System.out.println(hashtagList.toString());
		
		feedHashtagQueryDsl.feedHashtagDeleteByFid(fid);
		for(Hashtag h: hashtagList) {
			content = h.getContent();
			System.out.println();
			
			if(hashtagRepository.findByContent(content)!=null) {
				hid = hashtagRepository.findByContent(content).getId();
			}else {
				Hashtag ht = new Hashtag();
				ht.setContent(content);
				hashtag = hashtagRepository.save(ht);
				hid = hashtag.getId();
			}
			
			feedHashtag.setFeedHashtagkey(new FeedHashtagKey(fid, hid));
			feedHashtagRepository.save(feedHashtag);
		}
		
		return hashtagList;
	}

	@Override
	public List<Feed> searchByHashtag(String keyword) {
		return feedQueryDsl.searchByHashtag(keyword);
	}

	@Override
	public List<Feed> searchByUserID(int uid) {
		return feedQueryDsl.searchByUserID(uid);
	}

	@Override
	public List<Hashtag> findHashtagByKeyword(String keyword) {
		return hashtagQueryDsl.findHashtagByKeyword(keyword);
	}

	@Override
	public Long countFeedByHashtag(int id) {
		return hashtagQueryDsl.countFeedByHashtag(id);
	}

	@Override
	public Long countFeedByUser(int user_id) {
		return feedQueryDsl.countFeedByUser(user_id);

	}


	
	
	
	
	
}
