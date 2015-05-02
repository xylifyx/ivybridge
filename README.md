ivywagon
========

Exposing ivy as a maven repository protocol. Let you set the patterns, branch and other parameters for the ivy resolver in the ivybridge URL.

    <repository>
        <id>ivyrepo</id>
        <url>ivybridge:file:///Users/emartino/NetBeansProjects/ivybridge/ivybridge-facade/src/test/resources/ivyrepo/?branch=trunk&amp;ivypattern=([branch]/)[organisation]/[module]/[revision]/[type]/ivy-[revision].xml&amp;artpattern=([branch]/)[organisation]/[module]/[revision]/[type]/[artifact]-[revision].[ext]</url>
    </repository>

Requests for pom files is translated into an Ivy resolve and uses ivy's builtin pom generation. Requests for jar files are taken directly from the ivy cache. The Ivy model and Mavens project model are not isomorphic, so some heuristics is required to map those to each other. This is work in progress.
