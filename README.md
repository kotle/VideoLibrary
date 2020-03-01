# VideoLibrary
音视频播放库，目前基于ExoPlayer.

# 如何使用
1.创建带有生命感知的播放器，在activity销毁后会自己清理数据，activity不可见的时候暂停播放。
```kotlin
fun createLifecycleSimplePlayer(lifecycle: AppCompatActivity): SimplePlayer

fun createLifecycleSimplePlayer(lifecycle: Fragment): SimplePlayer
```
2.创建一般普通对象
```
SimplePlayer(
    private val context: Context,
    private val playerImpl: BaseYzsPlayer = ExoPlayerImpl(context)
) 
```

3.开始调用类[IYzsPlayer](https://github.com/kotle/VideoLibrary/blob/master/playerlibrary/src/main/java/com/yizisu/playerlibrary/IYzsPlayer.kt)中的方法<br/>
调用<br/>
**prepare(models: MutableList<PlayerModel>, playIndex: Int = 0, listener: Function1<PlayerModel?, Unit>? = null)**<br/>
或者<br/>
**prepareAndPlay(models: MutableList<PlayerModel>, playIndex: Int = 0, listener: Function1<PlayerModel?, Unit>? = null)**<br/>
开始播放<br/>
播放视频需要调用 **attachView(view: TextureView)** 设置视频展示的view
```kotlin
/**
     * 开始播放
     */
    fun play(listener: Function1<PlayerModel?, Unit>? = null)

    /**
     * 准备资源
     * 需要手动再调用播放
     */
    fun prepare(
        models: MutableList<PlayerModel>,
        playIndex: Int = 0,
        listener: Function1<PlayerModel?, Unit>? = null
    )

    /**
     * 准备完毕就播放
     */
    fun prepareAndPlay(
        models: MutableList<PlayerModel>,
        playIndex: Int = 0,
        listener: Function1<PlayerModel?, Unit>? = null
    )

    /**
     * 暂停播放
     */
    fun pause(listener: Function1<PlayerModel?, Unit>? = null)


    /**
     * 停止播放
     */
    fun stop(listener: Function1<PlayerModel?, Unit>? = null)

    /**
     * 下一个
     */
    fun next(listener: Function1<PlayerModel?, Unit>? = null)

    /**
     * 上一个
     */
    fun previous(listener: Function1<PlayerModel?, Unit>? = null)

    /**
     * 设置一个界面
     */
    fun attachView(view: TextureView)

    /**
     * 跳转
     */
    fun seekTo(
        positionMs: Long,
        index: Int? = null,
        listener: Function1<PlayerModel?, Unit>? = null
    )

    /**
     * 添加监听
     */
    fun addPlayerListener(listener: SimplePlayerListener)

    /**
     * 移除监听
     */
    fun removePlayerListener(listener: SimplePlayerListener)

    /**
     * 当前播放的model对象
     */
    fun getCurrentModel(): PlayerModel?

    /**
     * 获取播放列表
     */
    fun getAllPlayModel(): MutableList<PlayerModel>

    /**
     * 当前播放索引
     */
    fun getCurrentPlayIndex(): Int
```
