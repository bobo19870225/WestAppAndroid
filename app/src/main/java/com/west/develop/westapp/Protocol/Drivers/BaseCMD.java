package com.west.develop.westapp.Protocol.Drivers;

/**
 * Created by Develop0 on 2018/1/12.
 */
public abstract class BaseCMD {

    /**
     * 解密返回值
     *          0x0A  通读错误
     *          0x0E  硬件序列号错
     *          0x0D  超时
     *          0x0C  数据CRC错
     *          0x0F  写入错
     *          0x10  写入成功
     *          0x11  命令不支持
     *          0x13  电源控制错
     *          0x0B  命令完成或序列号正确
     *          0x14  不匹配
     */
    public static final byte CHK_BACK_COM_ERROR = 0x0A;
    public static final byte CHK_BACK_SN_ERROR = (byte)0x0E;
    public static final byte CHK_BACK_TIMEOUT = (byte)0x0D;
    public static final byte CHK_BACK_CRC_ERROR = (byte)0x0C;
    public static final byte CHK_BACK_WRITE_ERROR = (byte)0x0F;
    public static final byte CHK_BACK_WTITE_SUCCESS = (byte)0x10;
    public static final byte CHK_BACK_CMD_UNSURPPOT = (byte)0x11;
    public static final byte CHK_BACK_CMD_POWER_ERROR = (byte)0x13;
    public static final byte CHK_BACK_CMD_MISMATCH = (byte)0x14;
    public static final byte CHK_BACK_CMD_SUCCESS = (byte)0x0B;

    /**
     * 进入Boot 模式
     */
    public static final byte CMD_USER_TO_BOOT = (byte)0xC4;
    /**
     * 进入Boot 返回
     * 返回 主、从 CPU ID
     */
    public static final byte CMD_USER_TO_BOOT_BACK = (byte)0xA5;


    /**
     * 读取固件版本
     * 正确回复：{@CMD_READ_VER_BACK}
     */
    public static final byte CMD_READ_VER = (byte)0xC8;

    /**
     * 读取固件版本返回
     */
    public static final byte CMD_READ_VER_BACK = (byte)0xA8;

    /**
     * 校验设备序列号
     * 正确回复：{@BACK_CHECK_TRUE}
     */
    public static final byte CMD_CHECK_SN = (byte)0x01;

    /**
     * 准备写入
     * 正确回复：{@BACK_CHECK_TRUE}
     */
    public static final byte CMD_READY_PRO = (byte)0x02;

    /**
     * 下载本地调试程序，不需要手持机不需要解密
     */
    public static final byte CMD_WRITE_TYPE_DEBUG = (byte)0x05;

    /**
     * 读取设备序列号
     */
    public static final byte CMD_READ_SN = (byte)0xC9;
    /**
     * 读取设备序列号返回
     * 返回 设备序列号 取反
     */
    static final byte CMD_READ_SN_BACK = (byte)0xA9;

    static final byte CMD_DOWNLOAD_SHORT = (byte)0x03;

    /**
     * 下载程序 {每包 256 字节}
     * {@CMD_DOWNLOAD_WRITE}  0xFF {@CONTENT 256 byte} {@CRC 2 byte}
     */
    public static final byte CMD_DOWNLOAD_WRITE = (byte)0x09;

    /**
     * {@CMD_DOWNLOAD_WRITE} 文件过大
     */
    public static final byte CMD_DOWNLOAD_BACK_LARGE = (byte)0xF0;

    /**
     * 下载校验 / Boot 转 APP
     */
    public static final byte CMD_CHECK_DOWNLOAD_CRC = (byte)0x0B;
    /**
     * {@CMD_CHECK_DOWNLOAD_CRC} 下载校验 正确
     */
    public static final byte CMD_CHECK_DOWNLOAD_CRC_TRUE = (byte)0x57;
    /**
     * 读取下载 的校验值
     */
    public  static final byte CMD_READ_DOWNLOAD_CRC = (byte)0x05;

    /**
     * 执行函数
     * {@CMD_RUN_FUNC} + {函数(0x01 - 0x08)}
     */
    public static final byte CMD_RUN_FUNC = (byte)0xF0;

    /**
     * 显示内容
     */
    public static final byte CMD_DISPLAY = (byte)0x31;

    /**
     * {@CMD_RUN_FUNC} 所执行的函数 执行完成
     */
    public static final byte CMD_FINISH_FUNC = (byte)0xF1;

    public static final byte CMD_EXIT_FUNC = (byte)0xF2;

    /**
     * 键盘事件
     * {@CMD_KEY_EVENT} {@KEY_VALUE}
     * @KEY_VALUE:
     *              UP      {@KeyEvent.KEY_UP}
     *              DOWN    {@KeyEvent.KEY_DOWN}
     *              LEFT    {@KeyEvent.KEY_LEFT}
     *              RIGHT   {@KeyEvent.KEY_RIGHT}
     *              YES     {@KeyEvent.KEY_YES}
     *              ESC     {@KeyEvent.KEY_ESC}
     *              RELEASE {@KeyEvent.KEY_RELEASE}
     */
    public static final byte CMD_KEY_EVENT = (byte)0x30;




    public static final byte CMD_UPFILE_NEW = (byte)0x40;

    public static final byte CMD_UPFILE_DATA = (byte)0x41;

    public static final byte CMD_UPFILE_FINISH = (byte)0x42;

    public static final byte CMD_LOAD_BACKUP_NEW = (byte)0x43;

    public static final byte CMD_LOAD_BACKUP_LEN = (byte)0x43;

    public static final byte CMD_LOAD_BACKUP_START = (byte)0x44;

    public static final byte CMD_LOAD_BACKUP_DATA = (byte)0x44;

    public static final byte CMD_CLEAR_TIMEOUT = (byte)0x46;

    public static final byte CMD_UPFILE_CANCEL = (byte)0x47;

    public static final byte CMD_UPFILE_READY = (byte)0x0B;

}
