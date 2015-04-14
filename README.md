# DribSearch
android search to line anim

dribbble 上有许多创意的效果 这个控件的效果就是dribble上的一个实现 地址在这里

gist之前也有人贴出android 5.0的实现方式，解析矢量图然后通过path动画的实现， 地址在这里，
当然5.0以下肯定也是可以， 这个控件就是在2.3+ 的实现方式


效果图：

![效果图][1]

avi转gif 质量不是很好， 凑合着看吧

使用：

```
<org.liuyichen.dribsearch.DribSearchView
    android:id="@+id/dribSearchView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:paddingTop="18dp"
    android:paddingBottom="18dp"
    app:breadth="2"
    app:search_color="#fff"/>
    
    
    
dribSearchView = (DribSearchView) findViewById(R.id.dribSearchView);

dribSearchView.changeLine();

dribSearchView.changeSearch();
```

[1]: https://raw.githubusercontent.com/liuchenx/DribSearch/master/art/demo.gif

