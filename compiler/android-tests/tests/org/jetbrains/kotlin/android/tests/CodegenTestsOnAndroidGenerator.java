/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.android.tests;

import com.google.common.collect.Lists;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.UsefulTestCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.backend.common.output.OutputFileCollection;
import org.jetbrains.kotlin.cli.common.output.outputUtils.OutputUtilsKt;
import org.jetbrains.kotlin.cli.jvm.compiler.JvmPackagePartProvider;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment;
import org.jetbrains.kotlin.codegen.CodegenTestFiles;
import org.jetbrains.kotlin.codegen.GenerationUtils;
import org.jetbrains.kotlin.codegen.forTestCompile.ForTestCompileRuntime;
import org.jetbrains.kotlin.idea.KotlinFileType;
import org.jetbrains.kotlin.load.java.JvmAbi;
import org.jetbrains.kotlin.load.kotlin.PackagePartClassUtils;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.test.ConfigurationKind;
import org.jetbrains.kotlin.test.InTextDirectivesUtils;
import org.jetbrains.kotlin.test.KotlinTestUtils;
import org.jetbrains.kotlin.test.TestJdkKind;
import org.jetbrains.kotlin.utils.Printer;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodegenTestsOnAndroidGenerator extends UsefulTestCase {

    private final PathManager pathManager;
    private static final String testClassPackage = "org.jetbrains.kotlin.android.tests";
    private static final String testClassName = "CodegenTestCaseOnAndroid";
    private static final String baseTestClassPackage = "org.jetbrains.kotlin.android.tests";
    private static final String baseTestClassName = "AbstractCodegenTestCaseOnAndroid";
    private static final String generatorName = "CodegenTestsOnAndroidGenerator";

    private static final String [] FILE_NAME_ANNOTATIONS = new String [] {"@file:JvmName", "@file:kotlin.jvm.JvmName"};

    private final Pattern packagePattern = Pattern.compile("package (.*)");

    private final List<String> generatedTestNames = Lists.newArrayList();

    public static void generate(PathManager pathManager) throws Throwable {
        new CodegenTestsOnAndroidGenerator(pathManager).generateOutputFiles();
    }

    private CodegenTestsOnAndroidGenerator(PathManager pathManager) {
        this.pathManager = pathManager;
    }

    private void generateOutputFiles() throws Throwable {
        prepareAndroidModule();
        generateAndSave();
    }

    private void prepareAndroidModule() throws IOException {
        System.out.println("Copying kotlin-runtime.jar and kotlin-reflect.jar in android module...");
        copyKotlinRuntimeJars();

        System.out.println("Check \"libs\" folder in tested android module...");
        File libsFolderInTestedModule = new File(pathManager.getLibsFolderInAndroidTestedModuleTmpFolder());
        if (!libsFolderInTestedModule.exists()) {
            libsFolderInTestedModule.mkdirs();
        }
    }

    private void copyKotlinRuntimeJars() throws IOException {
        FileUtil.copy(
                ForTestCompileRuntime.runtimeJarForTests(),
                new File(pathManager.getLibsFolderInAndroidTmpFolder() + "/kotlin-runtime.jar")
        );
        FileUtil.copy(
                ForTestCompileRuntime.reflectJarForTests(),
                new File(pathManager.getLibsFolderInAndroidTmpFolder() + "/kotlin-reflect.jar")
        );

        FileUtil.copy(
                ForTestCompileRuntime.kotlinTestJarForTests(),
                new File(pathManager.getLibsFolderInAndroidTmpFolder() + "/kotlin-test.jar")
        );
    }

    private void generateAndSave() throws Throwable {
        System.out.println("Generating test files...");
        StringBuilder out = new StringBuilder();
        Printer p = new Printer(out);

        p.print(FileUtil.loadFile(new File("license/LICENSE.txt")));
        p.println("package " + testClassPackage + ";");
        p.println();
        p.println("import ", baseTestClassPackage, ".", baseTestClassName, ";");
        p.println();
        p.println("/* This class is generated by " + generatorName + ". DO NOT MODIFY MANUALLY */");
        p.println("public class ", testClassName, " extends ", baseTestClassName, " {");
        p.pushIndent();

        generateTestMethodsForDirectories(p, new File("compiler/testData/codegen/box"));

        p.popIndent();
        p.println("}");

        String testSourceFilePath =
                pathManager.getSrcFolderInAndroidTmpFolder() + "/" + testClassPackage.replace(".", "/") + "/" + testClassName + ".java";
        FileUtil.writeToFile(new File(testSourceFilePath), out.toString());
    }

    private void generateTestMethodsForDirectories(Printer p, File... dirs) throws IOException {
        FilesWriter holderMock = new FilesWriter(false);
        FilesWriter holderFull = new FilesWriter(true);

        for (File dir : dirs) {
            File[] files = dir.listFiles();
            Assert.assertNotNull("Folder with testData is empty: " + dir.getAbsolutePath(), files);
            processFiles(p, files, holderFull, holderMock);
        }

        holderFull.writeFilesOnDisk();
        holderMock.writeFilesOnDisk();
    }

    private class FilesWriter {
        private final boolean isFullJdkAndRuntime;

        public List<KtFile> files = new ArrayList<KtFile>();
        private KotlinCoreEnvironment environment;

        private FilesWriter(boolean isFullJdkAndRuntime) {
            this.isFullJdkAndRuntime = isFullJdkAndRuntime;
            environment = createEnvironment(isFullJdkAndRuntime);
        }

        private KotlinCoreEnvironment createEnvironment(boolean isFullJdkAndRuntime) {
            return isFullJdkAndRuntime ?
                   KotlinTestUtils.createEnvironmentWithJdkAndNullabilityAnnotationsFromIdea(
                           myTestRootDisposable, ConfigurationKind.ALL, TestJdkKind.FULL_JDK
                   ) :
                   KotlinTestUtils.createEnvironmentWithMockJdkAndIdeaAnnotations(myTestRootDisposable, ConfigurationKind.JDK_ONLY);
        }

        public boolean shouldWriteFilesOnDisk() {
            return files.size() > 300;
        }

        public void writeFilesOnDiskIfNeeded() {
            if (shouldWriteFilesOnDisk()) {
                writeFilesOnDisk();
            }
        }

        public void writeFilesOnDisk() {
            writeFiles(files);
            files = new ArrayList<KtFile>();
            environment = createEnvironment(isFullJdkAndRuntime);
        }

        private void writeFiles(List<KtFile> filesToCompile) {
            if (filesToCompile.isEmpty()) return;

            System.out.println("Generating " + filesToCompile.size() + " files" + (isFullJdkAndRuntime
                                                                                   ? " (full jdk and runtime)" : "") + "...");
            OutputFileCollection outputFiles;
            try {
                outputFiles = GenerationUtils.compileManyFilesGetGenerationStateForTest(
                        filesToCompile.iterator().next().getProject(),
                        filesToCompile,
                        new JvmPackagePartProvider(environment),
                        null
                ).getFactory();
            }
            catch (Throwable e) {
                throw new RuntimeException(e);
            }

            File outputDir = new File(pathManager.getOutputForCompiledFiles());
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            Assert.assertTrue("Cannot create directory for compiled files", outputDir.exists());

            OutputUtilsKt.writeAllTo(outputFiles, outputDir);
        }
    }

    private void processFiles(
            @NotNull Printer printer,
            @NotNull File[] files,
            @NotNull FilesWriter holderFull,
            @NotNull FilesWriter holderMock)
            throws IOException
    {
        holderFull.writeFilesOnDiskIfNeeded();
        holderMock.writeFilesOnDiskIfNeeded();

        for (File file : files) {
            if (SpecialFiles.getExcludedFiles().contains(file.getName())) {
                continue;
            }
            if (file.isDirectory()) {
                File[] listFiles = file.listFiles();
                if (listFiles != null) {
                    processFiles(printer, listFiles, holderFull, holderMock);
                }
            }
            else if (!FileUtilRt.getExtension(file.getName()).equals(KotlinFileType.INSTANCE.getDefaultExtension())) {
                // skip non kotlin files
            }
            else {
                String text = FileUtil.loadFile(file, true);
                //TODO: support multifile tests
                if (text.contains("FILE:")) continue;

                if (InTextDirectivesUtils.isDirectiveDefined(text, "WITH_REFLECT")) continue;

                if (hasBoxMethod(text)) {
                    String generatedTestName = generateTestName(file.getName());
                    String packageName = file.getPath().replaceAll("\\\\|-|\\.|/", "_");
                    Ref<FqName> oldPackage = new Ref();
                    text = changePackage(packageName, text, oldPackage);
                    FqName className = getGeneratedClassName(file, text, packageName);
                    text = patchClassForName(className, oldPackage.get(), text);

                    FilesWriter filesHolder = InTextDirectivesUtils.isDirectiveDefined(text, "FULL_JDK") ||
                                              InTextDirectivesUtils.isDirectiveDefined(text, "WITH_RUNTIME") ? holderFull : holderMock;
                    CodegenTestFiles codegenFile = CodegenTestFiles.create(file.getName(), text, filesHolder.environment.getProject());
                    filesHolder.files.add(codegenFile.getPsiFile());


                    generateTestMethod(printer, generatedTestName, className.asString(), StringUtil.escapeStringCharacters(file.getPath()));
                }
            }
        }
    }

    private static FqName getGeneratedClassName(File file, String text, String packageName) {
        FqName packageFqName = new FqName(packageName);
        for (String annotation : FILE_NAME_ANNOTATIONS) {
            if (text.contains(annotation)) {
                int indexOf = text.indexOf(annotation);
                return packageFqName.child(Name.identifier(text.substring(text.indexOf("(\"", indexOf) + 2, text.indexOf("\")", indexOf))));
            }
        }

        return PackagePartClassUtils.getPackagePartFqName(packageFqName, file.getName());
    }

    private static boolean hasBoxMethod(String text) {
        return text.contains("fun box()");
    }

    private String changePackage(String packageName, String text, Ref<FqName> oldPackage) {
        if (text.contains("package ")) {
            Matcher matcher = packagePattern.matcher(text);
            assert matcher.find();
            String group = matcher.toMatchResult().group(1);
            oldPackage.set(FqName.fromSegments(Arrays.asList(group.split("\\."))));
            return matcher.replaceAll("package " + packageName);
        }
        else {
            oldPackage.set(FqName.ROOT);
            String packageDirective = "package " + packageName + ";\n";
            if (text.contains("@file:")) {
                int index = text.lastIndexOf("@file:");
                int packageDirectiveIndex = text.indexOf("\n", index);
                return text.substring(0, packageDirectiveIndex + 1) + packageDirective + text.substring(packageDirectiveIndex + 1);
            } else {
                return packageDirective + text;
            }
        }
    }

    private static String patchClassForName(FqName className, FqName oldPackage, String text) {
        return text.replaceAll("Class\\.forName\\(\"" + oldPackage.child(className.shortName()).asString() + "\"\\)", "Class.forName(\"" + className.asString() + "\")");
    }

    private static void generateTestMethod(Printer p, String testName, String className, String filePath) {
        p.println("public void test" + testName + "() throws Exception {");
        p.pushIndent();
        p.println("invokeBoxMethod(" + className + ".class, \"" + filePath + "\", \"OK\");");
        p.popIndent();
        p.println("}");
        p.println();
    }

    private String generateTestName(String fileName) {
        String result = JvmAbi.sanitizeAsJavaIdentifier(FileUtil.getNameWithoutExtension(StringUtil.capitalize(fileName)));

        int i = 0;
        while (generatedTestNames.contains(result)) {
            result += "_" + i++;
        }
        generatedTestNames.add(result);
        return result;
    }
}
