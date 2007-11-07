# Generated by Buildr 1.2.8, change to your liking
# Version number for this release
VERSION_NUMBER = "1.0"
# Version number for the next release
NEXT_VERSION = "1.0"
# Group identifier for your projects
GROUP = "diggwatch"
COPYRIGHT = "2007  Dustin Sallings"

# Specify Maven 2.0 remote repositories here, like this:
repositories.remote << "http://www.ibiblio.org/maven2/"

SPY_REPO="http://bleu.west.spy.net/~dustin/repo/"

TREE_VER=`hg identify`
if not $?.success?
  raise "Failed to identify tree."
end
puts "Tree version is #{TREE_VER}"

# m1 artifact
def m1(parts)
  group, id, type, version = parts.split /:/

  url=SPY_REPO + "#{group}/#{type}s/#{id}-#{version}.#{type}"
  rv=artifact(parts)
  download(rv => url)
  rv
end

runtime_jars=[m1("spy:spy:jar:2.4"), m1("spy:jwebkit:jar:3.1"),
  m1("spy:memcached:jar:2.0-pre5"), m1("spy:digg:jar:1.2"),
  m1("spy:xmlkit:jar:2.2.3"), m1("google:guice:jar:1.0"),
  m1("google:guice-servlet:jar:1.0")]
buildtime_jars=runtime_jars + ["servletapi:servletapi:jar:2.4"]

desc "The Diggwatch project"
define "diggwatch" do

  project.version = VERSION_NUMBER
  project.group = GROUP
  manifest["Implementation-Vendor"] = COPYRIGHT

  compile.with buildtime_jars
  resources.filter.using "tree.version" => TREE_VER

  package(:war).with :libs => runtime_jars
end
# vim: syntax=ruby et ts=2
