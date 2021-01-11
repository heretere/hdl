package me.conclure.example;

import com.heretere.hdl.dependency.maven.annotation.MavenDependency;
import com.heretere.hdl.dependency.maven.annotation.MavenRepository;
import com.heretere.hdl.relocation.annotation.Relocation;

@MavenRepository("http://repo.bristermitten.me/repository/maven-releases/")
@MavenDependency("me|conclure:event-builder:1.1.1")
@Relocation(from = "me|conclure|eventbuilder", to = "me|conclure|example|eventbuilder")
final class DependencyHolder {
}
