[![Build Status](https://github.com/soot-oss/soot/workflows/Soot%20CI/badge.svg?branch=develop)](https://github.com/soot-oss/soot/actions)
[![Gitpod Ready-to-Code](https://img.shields.io/badge/Gitpod-Ready--to--Code-blue?logo=gitpod)](https://gitpod.io/#https://github.com/soot-oss/soot) 



# 新增代码主要位于soot.jimple.spark.solver.myreflection包下 
核心实现逻辑在soot.jimple.spark.solver.myreflection.MyReflectionModel类中
目前已通过 Inheritance
       DuplicateName
        ArgsRefine
        Basic测试用例。
测试入口在src/test/java/neu/lab/tool/test/SparkMain.java，看下注释。点击运行就可以在控制台查看打印结果。可进入各个函数定义查看理解。
本实现使用Soot 4.2.1版本进行实现。主要是在soot develop分支最新的4.4.0-SNAPSHOT版本上写发现有bug》。。。。

**主要困难**：目前在RecvType测试用例没法正常传播String对象（当然，根据谭老师的要求，可能也不用传播String对象，对于Unknown的两个测试用例可以直接在类名或方法名未知的情况下进行推理。此外，没完全实现getMethods。因此剩余四个测试用例过不了。

 

# Using Soot? Let us know about it!
We are regularly applying for funding to help us maintain Soot. You can help us immensely by letting us know about [**projects that use Soot**](https://github.com/soot-oss/soot/wiki/Users-of-Soot), both commercially or in the form of research tools.

Also many thanks to [![JProfiler](https://www.ej-technologies.com/images/product_banners/jprofiler_small.png)](https://www.ej-technologies.com/products/jprofiler/overview.html) for supporting Soot with a free-to-use open source license!

# Soot supports Java 9 modules now!
Try and get involved in Soot's Java 9 bleeding edge developement.
## What works and is tested?
* Automatic modules (modules automatically created from jars in the module-path)
* Named modules
* Exploded modules
* Modular jar files
* Resolving modules in Soot's `ModuleScene`
* Spark
   
## What does not work yet?
 * Anonymous modules (mixing module- and class-path)
 * Multi-module jar files

# What is Soot?

Soot is a Java optimization framework. It provides four intermediate representations for analyzing and transforming Java bytecode:

* Baf: a streamlined representation of bytecode which is simple to manipulate.
* Jimple: a typed 3-address intermediate representation suitable for optimization.
* Shimple: an SSA variation of Jimple.
* Grimp: an aggregated version of Jimple suitable for decompilation and code inspection.

See https://soot-oss.github.io/soot for details.

# How do I get started with Soot?

We have some documentation on Soot in the [wiki](https://github.com/soot-oss/soot/wiki) and also a large range of [tutorials](https://github.com/soot-oss/soot/wiki/Tutorials) on Soot. 

For detailed information please also consider the Soot's [JavaDoc and Options](https://github.com/soot-oss/soot/wiki/Options-and-JavaDoc) Documentations.

# Including Soot in your Project

A Soot SNAPSHOT is currently built for each commit to the `develop` branch. You can include Soot as 
a dependency via Maven, Gradle, SBT, etc using the following coordinates:


```.xml
<dependencies>
  <dependency>
    <groupId>org.soot-oss</groupId>
    <artifactId>soot</artifactId>
    <version>4.3.0-SNAPSHOT</version>
  </dependency>
</dependencies>
<repositories>
  <repository>
      <id>sonatype-snapshots</id>
      <url>https://oss.sonatype.org/content/repositories/snapshots</url>
      <releases>
          <enabled>false</enabled>
      </releases>
  </repository>
</repositories>	
```

You can also obtain older builds of the `develop` branch. A complete listing of builds can be found on [Maven Central](https://oss.sonatype.org/content/repositories/snapshots/org/soot-oss/soot/).

# How do I obtain Soot without Maven?
**We recommend using Soot with Maven**

You can obtain the latest SNAPSHOT build of Soot [directly](https://oss.sonatype.org/content/repositories/snapshots/org/soot-oss/soot/).

The `soot-<RELEASE>-jar-with-dependencies.jar` file is an all-in-one file that also contains all the required libraries. 

The `soot-<RELEASE>.jar`  file contains only Soot, allowing you to manually pick dependencies as you need them. If you do not want to bother with dependencies, we recommend using the former.

# Building Soot yourself

If you cannot work with the prebuild versions and need to build Soot on your own, please consider the [wiki](https://github.com/soot-oss/soot/wiki/Building-Soot-from-the-Command-Line-(Recommended)) for further steps.

# About Soot's source code

Soot follows the git-flow convention. Releases and hotfixes are maintained in the master branch.
Development happens in the develop branch. To catch the bleeding edge of Soot, check out the latter.
In case of any questions, please consult the Soot
mailing list at: http://www.sable.mcgill.ca/mailman/listinfo/soot-list/

# How do I contribute to Soot?

We are happy to accept arbitrary improvements to Soot in form of GitHub pull requests. Please read our [contribution guidelines](https://github.com/soot-oss/soot/blob/master/CONTRIBUTING.md) before setting up a pull request.

# Please help us improve Soot!
You are using Soot and would like to help us support it in the future? Then please support us by filling out [this little web form](https://goo.gl/forms/rk1oSxFIxAH0xaf52).

That way you can help us in two ways:
* By letting us know how we can improve Soot you can directly help us prioritize newly planned features.
* By stating your name and affiliation you help us showcasing Soot’s large user base.
Thanks!

# How to use Soot's Java 9 Features?

If you want to run Soot with Java > 8, you are done. Just run it as usal.
If you want to execute Soot with Java 8 but analyze Java >8 Projects or vice versa, see below.

## Use from Source Code
To load modules in Soot's `ModuleScene` from java:
```.java
// configure Soot's options
Options.v().set_soot_modulepath(modulePath);


// load classes from modules into Soot
Map<String, List<String>> map = ModulePathSourceLocator.v().getClassUnderModulePath(modulePath);
for (String module : map.keySet()) {
   for (String klass : map.get(module)) {
       logger.info("Loaded Class: " + klass + "\n");
       loadClass(klass, false, module);

   }
}


//this must be called after all classes are loaded
Scene.v().loadNecessaryClasses();


public static SootClass loadClass(String name, boolean main, String module) {
     SootClass c = ModuleScene.v().loadClassAndSupport(name, Optional.of(module));
     c.setApplicationClass();
     if (main)
         Scene.v().setMainClass(c);
     return c;
}

```


### Example Configurations: Java 8, Java >= 9 Classpath, Java >= 9 Modulepath

```.java

if(java < 9 ) {
    Options.v().set_prepend_classpath(true);
    Options.v().set_process_dir(Arrays.asList(applicationClassPath().split(File.pathSeparator)));
    Options.v().set_claspath(sootClassPath();
}

if(java >= 9 && USE_CLASSPATH){
    Options.v().set_soot_classpath("VIRTUAL_FS_FOR_JDK" + File.pathSeparator + sootClassPath());
    Options.v().set_process_dir(Arrays.asList(applicationClassPath().split(File.pathSeparator)));
}


if(java>=9 && USE_MODULEPATH){
    Options.v().set_prepend_classpath(true);
    Options.v().set_soot_modulepath(ootClassPath());
    Options.v().set_process_dir(Arrays.asList(applicationClassPath().split(File.pathSeparator)));
}

```

## Use from the Command Line
To execute Soot using Java 1.9, but analyzing a classpath run, just as before:
`java -cp soot-trunk.jar soot.Main --process-dir directoryToAnalyse`


if you want to specify the classpath explicitly run:
`java -cp soot-trunk.jar soot.Main -cp VIRTUAL_FS_FOR_JDK --process-dir directoryToAnalyse`

the value `VIRTUAL_FS_FOR_JDK` indicates that Soot should search Java's (>9) virtual filesystem `jrt:/` for classes, too, although Soot is not executed in module mode.


To load modules and classes in Soot using java 1.8 run:

` java -cp PATH_TO_JAVA9/jrt-fs.jar:soot-trunk.jar soot.Main -pp -soot-modulepath modules/  `


Please replace `PATH_TO_JAVA9` with the path to your local installation of java 9.
The `jrt-fs.jar` is a built-in NIO FileSystem provider for the jrt:// filesystem java 9 uses that replaces `rt.jar`. 


