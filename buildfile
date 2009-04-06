# -*- ruby -*-
# Generated by Buildr 1.2.8, change to your liking
# Version number for this release
VERSION_NUMBER = "1.0"
# Version number for the next release
NEXT_VERSION = "1.0"
# Group identifier for your projects
GROUP = "spy"
COPYRIGHT = "2007  Dustin Sallings"

# Specify Maven 2.0 remote repositories here, like this:
repositories.remote << "http://www.ibiblio.org/maven2/"
repositories.remote << "http://bleu.west.spy.net/~dustin/m2repo/"

plugins=[
  'spy:m1compat:rake:1.0',
  'spy:git_tree_version:rake:1.0',
]

plugins.each do |spec|
  artifact(spec).tap do |plugin|
    plugin.invoke
    load plugin.name
  end
end

desc "The Diggwatch project"
define "diggwatch" do

  runtime_jars=[m1("spy:spy:jar:2.4"), m1("spy:jwebkit:jar:3.1"),
    m1("spy:memcached:jar:2.3.1"), m1("spy:digg:jar:1.3.6"),
    m1("spy:xmlkit:jar:2.2.3"), m1("google:guice:jar:1.0"),
    m1("google:guice-servlet:jar:1.0"),
    "taglibs:standard:jar:1.1.2", "jstl:jstl:jar:1.1.2"]
  buildtime_jars=runtime_jars + ["servletapi:servletapi:jar:2.4"]

  TREE_VER=tree_version
  puts "Tree version is #{TREE_VER}"

  project.version = VERSION_NUMBER
  project.group = GROUP
  manifest["Implementation-Vendor"] = COPYRIGHT

  compile.with buildtime_jars
  resources.filter.using "tree.version" => TREE_VER

  package(:war).with :libs => runtime_jars
end
# vim: syntax=ruby et ts=2
