
springboot的jar包能够加载其他路径，BOOT-INF，BOOT-classes等，是因为使用了其重定义的JarLoader， classloader也重写了

FileInputStream fis = new FileInputStream（“./aa.txt”）这种是从程序启动的当前路径开始寻找，如果是用classloader加载就是从classpath去寻找。
