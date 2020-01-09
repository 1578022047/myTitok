package com.example.dou.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.example.dou.App;
import com.example.dou.R;
import com.example.dou.bean.CommentEntity;
import com.example.dou.bean.CommentMoreBean;
import com.example.dou.bean.FirstLevelBean;
import com.example.dou.bean.SecondLevelBean;
import com.example.dou.dialog.InputTextMsgDialog;
import com.example.dou.multi.CommentDialogMutiAdapter;
import com.example.dou.pojo.Flag;
import com.example.dou.pojo.User;
import com.example.dou.pojo.Video;
import com.example.dou.ui.activity.UserVideoActivity;
import com.example.dou.ui.activity.VerifyLoginActivity;
import com.example.dou.utils.HttpUtil;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Response;

public class UserVideoAdapter extends RecyclerView.Adapter<UserVideoAdapter.ViewHolder> implements BaseQuickAdapter.RequestLoadMoreListener{

    private List<MultiItemEntity> data = new ArrayList<>();
    private List<FirstLevelBean> datas = new ArrayList<>();
    private BottomSheetDialog bottomSheetDialog;
    private InputTextMsgDialog inputTextMsgDialog;
    private float slideOffset = 0;
    private String content = "我听见你的声音，有种特别的感觉。让我不断想，不敢再忘记你。如果真的有一天，爱情理想会实现，我会加倍努力好好对你，永远不改变";
    private CommentDialogMutiAdapter bottomSheetAdapter;
    private RecyclerView rv_dialog_lists;
    private long totalCount = 22;
    private int offsetY;


    User user;
    User me;
    List<Flag> flags;
    private int play = 0;



    private List<Video> videos;
    Context context;
    int numberLike;
    public void setPlay(int play) {
        this.play = play;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        StandardGSYVideoPlayer video;
        ImageView heart;
        CircleImageView userImage;
        ImageView attention;
        TextView likeNum;
        TextView remarkNum;
        ImageView remark;

        public ViewHolder(View view){
            super(view);
            remark=view.findViewById(R.id.remark);
            video=view.findViewById(R.id.video);
            heart=view.findViewById(R.id.heart);
            userImage=view.findViewById(R.id.user_image);
            attention=view.findViewById(R.id.attention);
            likeNum=view.findViewById(R.id.likeNum);
            remarkNum=view.findViewById(R.id.remarkNum);
        }
    }

    public UserVideoAdapter(List<Video> videos, User user ,List<Flag> flags,Context context) {
        this.videos=videos;
        this.user=user;
        this.context=context;
        this.flags=flags;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.vedio_item,viewGroup,false);
        ViewHolder holder=new ViewHolder(view);
        me=((App)context.getApplicationContext()).getUser();
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.remark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show();
            }
        });
        initData();
        dataSort(0);
        showSheetDialog();


        String url=HttpUtil.host+"getLikeVideoNum";
        HttpUtil.getLikeVideoNumHttp(url,videos.get(i).getVideoId().toString(), new okhttp3.Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {

            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                try {
                    numberLike=Integer.parseInt(response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                new Handler(context.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if(numberLike!=0){
                            viewHolder.likeNum.setText(String.valueOf(numberLike));
                        }

                    }
                });
            }
        });

        if(flags.get(i).isLikeFlag()){
            viewHolder.heart.setImageResource(R.drawable.redheart);
        }
        if(flags.get(i).isAttentionFlag()){
            viewHolder.attention.setVisibility(View.GONE);
        }
        if(user==null) {
            viewHolder.heart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(new Intent(context,VerifyLoginActivity.class));
                }
            });
        }else {
            viewHolder.heart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if(flags.get(i).isLikeFlag()){
                        numberLike--;
                        if(numberLike==0){
                            viewHolder.likeNum.setText("赞");
                        }else {
                            viewHolder.likeNum.setText(String.valueOf(numberLike));
                        }
                        flags.get(i).setLikeFlag(false);
                        viewHolder.heart.setImageResource(R.drawable.heart);
                        String url=HttpUtil.host+"cancelLikeVideo";
                        HttpUtil.CancelLikeVideoHttp(url,user.getUserId(),videos.get(i).getVideoId().toString(), new okhttp3.Callback() {
                            @Override
                            public void onFailure(final Call call, final IOException e) {

                            }

                            @Override
                            public void onResponse(final Call call, final Response response) throws IOException {

                            }
                        });
                    }else{
                        numberLike++;
                        viewHolder.likeNum.setText(String.valueOf(numberLike));
                        flags.get(i).setLikeFlag(true);
                        viewHolder.heart.setImageResource(R.drawable.redheart);
                        String url=HttpUtil.host+"likeVideo";
                        HttpUtil.likeVideoHttp(url,user.getUserId(),videos.get(i).getVideoId().toString(), new okhttp3.Callback() {
                            @Override
                            public void onFailure(final Call call, final IOException e) {

                            }

                            @Override
                            public void onResponse(final Call call, final Response response) throws IOException {

                            }
                        });
                    }
                }
            });
        }
        if(user==null) {
            viewHolder.attention.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(new Intent(context,VerifyLoginActivity.class));
                }
            });
        }else {
            viewHolder.attention.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    flags.get(i).setAttentionFlag(true);
                    viewHolder.attention.setVisibility(View.GONE);
                    String url=HttpUtil.host+"attentionUser";

                    HttpUtil.attentionUserHttp(url,me.getUserId(),user.getUserId(), new okhttp3.Callback() {
                        @Override
                        public void onFailure(final Call call, final IOException e) {

                        }

                        @Override
                        public void onResponse(final Call call, final Response response) throws IOException {

                        }
                    });
                }
            });
        }

        Glide.with(context).load(user.getImageUrl())
                .into(viewHolder.userImage);
        viewHolder.userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                ((UserVideoActivity)context).finish();
            }
        });
        if (play == i) {
            viewHolder.video.setVisibility(View.VISIBLE);
            viewHolder.video.setLooping(true);
            viewHolder.video.setIsTouchWiget(false);
            viewHolder.video.getBackButton().setVisibility(View.GONE);
            viewHolder.video.getTitleTextView().setVisibility(View.GONE);
            viewHolder.video.getFullscreenButton().setVisibility(View.GONE);
            viewHolder.video.setDismissControlTime(0);
            viewHolder.video.setNeedShowWifiTip(true);
            viewHolder.video.setUp(videos.get(i).getVideoUrl(),true,videos.get(i).getVideoUrl());
            viewHolder.video.startPlayLogic();
            viewHolder.video.setReleaseWhenLossAudio(true);
        } else {
            viewHolder.video.setVisibility(View.GONE);
            viewHolder.video.release();
        }
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    private void initRefresh() {
        datas.clear();
        initData();
        dataSort(0);
        bottomSheetAdapter.setNewData(data);
    }

    //原始数据 一般是从服务器接口请求过来的
    private void initData() {
        int size = 10;
        for (int i = 0; i < size; i++) {
            FirstLevelBean firstLevelBean = new FirstLevelBean();
            firstLevelBean.setContent("第" + (i + 1) + "人评论内容" + (i % 3 == 0 ? content + (i + 1) + "次" : ""));
            firstLevelBean.setCreateTime(System.currentTimeMillis());
            firstLevelBean.setHeadImg("https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=3370302115,85956606&fm=26&gp=0.jpg");
            firstLevelBean.setId(i + "");
            firstLevelBean.setIsLike(0);
            firstLevelBean.setLikeCount(i);
            firstLevelBean.setUserName("星梦缘" + (i + 1));
            firstLevelBean.setTotalCount(i + size);

            List<SecondLevelBean> beans = new ArrayList<>();
            for (int j = 0; j < 10; j++) {
                SecondLevelBean secondLevelBean = new SecondLevelBean();
                secondLevelBean.setContent("一级第" + (i + 1) + "人 二级第" + (j + 1) + "人评论内容" + (j % 3 == 0 ? content + (j + 1) + "次" : ""));
                secondLevelBean.setCreateTime(System.currentTimeMillis());
                secondLevelBean.setHeadImg("https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=1918451189,3095768332&fm=26&gp=0.jpg");
                secondLevelBean.setId(j + "");
                secondLevelBean.setIsLike(0);
                secondLevelBean.setLikeCount(j);
                secondLevelBean.setUserName("星梦缘" + (i + 1) + "  " + (j + 1));
                secondLevelBean.setIsReply(j % 5 == 0 ? 1 : 0);
                secondLevelBean.setReplyUserName(j % 5 == 0 ? "闭嘴家族" + j : "");
                secondLevelBean.setTotalCount(firstLevelBean.getTotalCount());
                beans.add(secondLevelBean);
                firstLevelBean.setSecondLevelBeans(beans);
            }
            datas.add(firstLevelBean);
        }
    }

    /**
     * 对数据重新进行排列
     * 目的是为了让一级评论和二级评论同为item
     * 解决滑动卡顿问题
     * @param position
     */
    private void dataSort(int position) {
        if (datas.isEmpty()) {
            data.add(new MultiItemEntity() {
                @Override
                public int getItemType() {
                    return CommentEntity.TYPE_COMMENT_EMPTY;
                }
            });
            return;
        }

        if (position <= 0) data.clear();
        int posCount = data.size();
        int count = datas.size();
        for (int i = 0; i < count; i++) {
            if (i < position) continue;

            //一级评论
            FirstLevelBean firstLevelBean = datas.get(i);
            if (firstLevelBean == null) continue;
            firstLevelBean.setPosition(i);
            posCount += 2;
            List<SecondLevelBean> secondLevelBeans = firstLevelBean.getSecondLevelBeans();
            if (secondLevelBeans == null || secondLevelBeans.isEmpty()) {
                firstLevelBean.setPositionCount(posCount);
                data.add(firstLevelBean);
                continue;
            }
            int beanSize = secondLevelBeans.size();
            posCount += beanSize;
            firstLevelBean.setPositionCount(posCount);
            data.add(firstLevelBean);

            //二级评论
            for (int j = 0; j < beanSize; j++) {
                SecondLevelBean secondLevelBean = secondLevelBeans.get(j);
                secondLevelBean.setChildPosition(j);
                secondLevelBean.setPosition(i);
                secondLevelBean.setPositionCount(posCount);
                data.add(secondLevelBean);
            }

            //展示更多的item
            if (beanSize <= 18) {
                CommentMoreBean moreBean = new CommentMoreBean();
                moreBean.setPosition(i);
                moreBean.setPositionCount(posCount);
                moreBean.setTotalCount(firstLevelBean.getTotalCount());
                data.add(moreBean);
            }

        }
    }

    public void show() {
        bottomSheetAdapter.notifyDataSetChanged();
        slideOffset = 0;
        bottomSheetDialog.show();
    }


    private void showSheetDialog() {
        if (bottomSheetDialog != null) {
            return;
        }
        View view = View.inflate(context, R.layout.dialog_bottomsheet, null);
        ImageView iv_dialog_close = (ImageView) view.findViewById(R.id.dialog_bottomsheet_iv_close);
        rv_dialog_lists = (RecyclerView) view.findViewById(R.id.dialog_bottomsheet_rv_lists);
        RelativeLayout rl_comment = view.findViewById(R.id.rl_comment);

        iv_dialog_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });

        rl_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //添加二级评论
                initInputTextMsgDialog(null, false, null, -1);
            }
        });

        bottomSheetAdapter = new CommentDialogMutiAdapter(data);
        rv_dialog_lists.setHasFixedSize(true);
        rv_dialog_lists.setLayoutManager(new LinearLayoutManager(context));
        closeDefaultAnimator(rv_dialog_lists);
        bottomSheetAdapter.setOnLoadMoreListener(this, rv_dialog_lists);
        rv_dialog_lists.setAdapter(bottomSheetAdapter);

        bottomSheetAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view1, int position) {
                switch ((int) view1.getTag()) {
                    case CommentEntity.TYPE_COMMENT_PARENT:
                        if (view1.getId() == R.id.rl_group) {
                            //添加二级评论
                            initInputTextMsgDialog((View) view1.getParent(), false, bottomSheetAdapter.getData().get(position), position);
                        } else if (view1.getId() == R.id.ll_like) {
                            //一级评论点赞 项目中还得通知服务器 成功才可以修改
                            FirstLevelBean bean = (FirstLevelBean) bottomSheetAdapter.getData().get(position);
                            bean.setLikeCount(bean.getLikeCount() + (bean.getIsLike() == 0 ? 1 : -1));
                            bean.setIsLike(bean.getIsLike() == 0 ? 1 : 0);
                            datas.set(bean.getPosition(), bean);
                            dataSort(0);
                            bottomSheetAdapter.notifyDataSetChanged();
                        }
                        break;
                    case CommentEntity.TYPE_COMMENT_CHILD:

                        if (view1.getId() == R.id.rl_group) {
                            //添加二级评论（回复）
                            initInputTextMsgDialog(view1, true, bottomSheetAdapter.getData().get(position), position);
                        } else if (view1.getId() == R.id.ll_like) {
                            //二级评论点赞 项目中还得通知服务器 成功才可以修改
                            SecondLevelBean bean = (SecondLevelBean) bottomSheetAdapter.getData().get(position);
                            bean.setLikeCount(bean.getLikeCount() + (bean.getIsLike() == 0 ? 1 : -1));
                            bean.setIsLike(bean.getIsLike() == 0 ? 1 : 0);

                            List<SecondLevelBean> secondLevelBeans = datas.get((int) bean.getPosition()).getSecondLevelBeans();
                            secondLevelBeans.set(bean.getChildPosition(), bean);
//                            CommentMultiActivity.this.dataSort(0);
                            bottomSheetAdapter.notifyDataSetChanged();
                        }

                        break;
                    case CommentEntity.TYPE_COMMENT_MORE:
                        //在项目中是从服务器获取数据，其实就是二级评论分页获取
                        CommentMoreBean moreBean = (CommentMoreBean) bottomSheetAdapter.getData().get(position);
                        SecondLevelBean secondLevelBean = new SecondLevelBean();
                        secondLevelBean.setContent("more comment" + 1);
                        secondLevelBean.setCreateTime(System.currentTimeMillis());
                        secondLevelBean.setHeadImg("https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=1918451189,3095768332&fm=26&gp=0.jpg");
                        secondLevelBean.setId(1 + "");
                        secondLevelBean.setIsLike(0);
                        secondLevelBean.setLikeCount(0);
                        secondLevelBean.setUserName("星梦缘" + 1);
                        secondLevelBean.setIsReply(0);
                        secondLevelBean.setReplyUserName("闭嘴家族" + 1);
                        secondLevelBean.setTotalCount(moreBean.getTotalCount() + 1);

                        datas.get((int) moreBean.getPosition()).getSecondLevelBeans().add(secondLevelBean);
                        dataSort(0);
                        bottomSheetAdapter.notifyDataSetChanged();

                        break;
                    case CommentEntity.TYPE_COMMENT_EMPTY:
                        initRefresh();
                        break;

                }

            }
        });

        bottomSheetDialog = new BottomSheetDialog(context, R.style.dialog);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.setCanceledOnTouchOutside(true);
        final BottomSheetBehavior mDialogBehavior = BottomSheetBehavior.from((View) view.getParent());
        mDialogBehavior.setPeekHeight(getWindowHeight());

        //dialog滑动监听
        mDialogBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    mDialogBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                } else if (newState == BottomSheetBehavior.STATE_SETTLING) {
                    if (slideOffset <= -0.28) {
                        //当向下滑动时 值为负数
                        bottomSheetDialog.dismiss();
                    }
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                slideOffset = slideOffset;//记录滑动值
            }
        });
    }

    int positionCount = 0;

    private void initInputTextMsgDialog(View view, final boolean isReply, final MultiItemEntity item, final int position) {
        dismissInputDialog();
        if (view != null) {
            offsetY = view.getTop();
            scrollLocation(offsetY);
        }
        if (inputTextMsgDialog == null) {
            inputTextMsgDialog = new InputTextMsgDialog(context, R.style.dialog_center);
            inputTextMsgDialog.setmOnTextSendListener(new InputTextMsgDialog.OnTextSendListener() {
                @Override
                public void onTextSend(String msg) {
                    addComment(isReply,item,position,msg);
                }

                @Override
                public void dismiss() {
                    //item滑动到原位
                    scrollLocation(-offsetY);
                }
            });
        }
        showInputTextMsgDialog();
    }

    //添加评论
    private void addComment(boolean isReply, MultiItemEntity item, final int position,String msg){
        final String userName = "hui";
        if (position >= 0) {
            //添加二级评论
            int pos = 0;
            String replyUserName = "未知";
            if (item instanceof FirstLevelBean) {
                FirstLevelBean firstLevelBean = (FirstLevelBean) item;
                positionCount = (int) (firstLevelBean.getPositionCount() + 1);
                pos = (int) firstLevelBean.getPosition();
                replyUserName = firstLevelBean.getUserName();
            } else if (item instanceof SecondLevelBean) {
                SecondLevelBean secondLevelBean = (SecondLevelBean) item;
                positionCount = (int) (secondLevelBean.getPositionCount() + 1);
                pos = (int) secondLevelBean.getPosition();
                replyUserName = secondLevelBean.getUserName();
            }

            SecondLevelBean secondLevelBean = new SecondLevelBean();
            secondLevelBean.setReplyUserName(replyUserName);
            secondLevelBean.setIsReply(isReply ? 1 : 0);
            secondLevelBean.setContent(msg);
            secondLevelBean.setHeadImg("https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=3370302115,85956606&fm=26&gp=0.jpg");
            secondLevelBean.setCreateTime(System.currentTimeMillis());
            secondLevelBean.setIsLike(0);
            secondLevelBean.setUserName(userName);
            secondLevelBean.setId("");
            secondLevelBean.setPosition(positionCount);

            datas.get(pos).getSecondLevelBeans().add(secondLevelBean);
            dataSort(0);
            bottomSheetAdapter.notifyDataSetChanged();
            rv_dialog_lists.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ((LinearLayoutManager) rv_dialog_lists.getLayoutManager())
                            .scrollToPositionWithOffset(positionCount >= data.size() - 1 ? data.size() - 1
                                    : positionCount, positionCount >= data.size() - 1 ? Integer.MIN_VALUE : rv_dialog_lists.getHeight());
                }
            }, 100);

        } else {
            //添加一级评论
            FirstLevelBean firstLevelBean = new FirstLevelBean();
            firstLevelBean.setUserName(userName);
            firstLevelBean.setId(bottomSheetAdapter.getItemCount() + 1 + "");
            firstLevelBean.setHeadImg("https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=1918451189,3095768332&fm=26&gp=0.jpg");
            firstLevelBean.setCreateTime(System.currentTimeMillis());
            firstLevelBean.setContent(msg);
            firstLevelBean.setLikeCount(0);
            firstLevelBean.setSecondLevelBeans(new ArrayList<SecondLevelBean>());
            datas.add(0, firstLevelBean);
            dataSort(0);
            bottomSheetAdapter.notifyDataSetChanged();
            rv_dialog_lists.scrollToPosition(0);
        }
    }

    private void dismissInputDialog() {
        if (inputTextMsgDialog != null) {
            if (inputTextMsgDialog.isShowing()) inputTextMsgDialog.dismiss();
            inputTextMsgDialog.cancel();
            inputTextMsgDialog = null;
        }
    }

    private void showInputTextMsgDialog() {
        inputTextMsgDialog.show();
    }

    private int getWindowHeight() {
        Resources res = context.getResources();
        DisplayMetrics displayMetrics = res.getDisplayMetrics();
        return displayMetrics.heightPixels;
    }

    @Override
    public void onLoadMoreRequested() {
        if (datas.size() >= totalCount) {
            bottomSheetAdapter.loadMoreEnd(false);
            return;
        }
        FirstLevelBean firstLevelBean = new FirstLevelBean();
        firstLevelBean.setUserName("hui");
        firstLevelBean.setId((datas.size() + 1) + "");
        firstLevelBean.setHeadImg("https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=1918451189,3095768332&fm=26&gp=0.jpg");
        firstLevelBean.setCreateTime(System.currentTimeMillis());
        firstLevelBean.setContent("add loadmore comment");
        firstLevelBean.setLikeCount(0);
        firstLevelBean.setSecondLevelBeans(new ArrayList<SecondLevelBean>());
        datas.add(firstLevelBean);
        dataSort(datas.size() - 1);
        bottomSheetAdapter.notifyDataSetChanged();
        bottomSheetAdapter.loadMoreComplete();

    }

    // item滑动
    public void scrollLocation(int offsetY) {
        try {
            rv_dialog_lists.smoothScrollBy(0, offsetY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭默认局部刷新动画
     */
    public void closeDefaultAnimator(RecyclerView mRvCustomer) {
        if (null == mRvCustomer) return;
        mRvCustomer.getItemAnimator().setAddDuration(0);
        mRvCustomer.getItemAnimator().setChangeDuration(0);
        mRvCustomer.getItemAnimator().setMoveDuration(0);
        mRvCustomer.getItemAnimator().setRemoveDuration(0);
        ((SimpleItemAnimator) mRvCustomer.getItemAnimator()).setSupportsChangeAnimations(false);
    }

}

