## repository

#### dependencies

- `pom.xml` 파일에 `<dependency>` 태그를 사용하여 아래 라이브러리 추가
- `pom.xml` 파일에 다음과 같이 `<dependency>` 태그를 추가하여 의존성을 지정할 수 있음

```xml
<!-- library 추가 -->
<dependency>
    <groupId>hjkim27.dev.lib</groupId>
    <artifactId>java-utils</artifactId>
    <version>0.0.4-SNAPSHOT</version>
</dependency>
```

#### repositories

- 위 의존성은 Maven이 해당 라이브러리를 로컬 저장소에서 찾을 수 있도록 추가해야 함.
- 로컬 저장소는 프로젝트의 `repo` 디렉토리에 위치하며, 이 디렉토리는 프로젝트의 루트 디렉토리와 동일한 위치에 있어야 함
- 파일 경로 : `repo/hjkim27/dev/lib/java-utils/0.0.4-SHAPSHOT/java-utils-0.0.4-SNAPSHOT.jar`

```xml
<!-- repo 추가 -->
<repository>
    <id>localrepository</id>
    <url>file://${project.basedir}/repo</url>
</repository>
```