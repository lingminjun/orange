package com.ssn.framework.uikit.inc;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

import java.util.List;

/**
 * 所有界面实体都走此声明周期
 * Created by lingminjun on 15/7/8.
 */
public interface ViewController {

    /**
     * 初始化方法，此方法一般不建议重载
     */
//    public void onInit();

    /**
     * 页面创建，此方法只调用一次，初始化时，不能加载view的内容
     */
//    public void onCreateController();

    /**
     * 主view
     * @return
     */
//    public View containerView();

    /**
     * 加载当前页面，此界面的主view
     * @param inflater
     */
    public View loadView(LayoutInflater inflater);

    /**
     * 当此页面的containerView被成功加载后回调，方法只会调用一次
     * @return 返回当前此视图主要的view
     */
    public void onViewDidLoad();

    /**
     * 页面将要被展示
     * //@param animated 展示是否启动动画
     */
//    public void onViewWillAppear(/*boolean animated*/);

    /**
     * 页面已经被展示，展示subView，可以得到所有subView尺寸与位置
     * //@param animated 展示是否启动动画
     */
    public void onViewDidAppear(/*boolean animated*/);

    /**
     * Activit已经被消失
     * //@param animated 展示是否启动动画
     */
    public void onViewDidDisappear(/*boolean animated*/);

    /**
     * Activit进入后台
     */
    public void onDidEnterBackgroud();

    /**
     * 页面结束，与onViewDidLoad对应，此时可以释放view以及subView和其他资源
     */
//    public void onViewDidUnload();

    /**
     * 系统内存资源不足是调用，释放掉不用的资源，4.0以前版本level == TRIM_MEMORY_UI_HIDDEN
     */
    public void onReceiveMemoryWarning(int level);

    /**
     * 页面退出，此方法与onCreateController对应
     */
    public void onDestroyController();

    /**
     * view是否显示
     * @return
     */
    public boolean isViewVisible();


    /**
     * 获取父容器，如果没有返回null
     * @return
     */
    public ContainerViewController containerController();


    public void onControllerActivityResult(int requestCode, int resultCode, Intent data);

    /**
     * 容器控制必须实现接口
     */
    public static interface ContainerViewController {
        /**
         * 当前显示的viewController
         * @return
         */
        public ViewController topViewController();

        /**
         * 包涵的被包含的子viewController
         * @return
         */
        public List<ViewController> childrenViewControllers();
    }
}
