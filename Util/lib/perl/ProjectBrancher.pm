###############################################################################
##
##  This package provides utilities to interact with the EuPathDB
##  Subversion repository.
##
###############################################################################

package FgpUtil::Util::ProjectBrancher;

use base 'Exporter';
use strict;
use warnings;

our @EXPORT = qw(getRepoBaseUrl getCmd getProjectGroup getProjectGroupOptions printProjectGroupInfo);

# Base URL for EuPathDB Subversion repositories
my $url = 'https://cbilsvn.pmacs.upenn.edu/svn';

# Base projects
my @baseProjects = ('install:gus', 'CBIL:gus', 'FgpUtil:gus', 'WSF:gus', 'WDK:gus', 'TuningManager:gus');

# Backend/data projects
my @backendProjects = ('GusAppFramework:gus', 'GusSchema:gus', 'ReFlow:gus',
                       'DoTS:apidb', 'GGTools:apidb','TuningManager:gus',
                       'ApiCommonData:apidb', 'ApiCommonWorkflow:apidb',
                       'ApiCommonModel:apidb', 'ApiCommonDatasets:apidb',
                       'OrthoMCLData:apidb', 'OrthoMCLWorkflow:apidb', 
                       'OrthoMCLModel:apidb', 'OrthoMCLEngine:apidb',
                       'OrthoMCLDatasets:apidb', 'ApiCommonMetadataRepository:apidb');

# Shared website projects
my @sharedSiteProjects = ('EbrcModelCommon:apidb', 'EbrcWebsiteCommon:apidb', 'EbrcWebSvcCommon:apidb');

# Additional projects for Api sites
my @apiSiteProjects = ('ApiCommonPresenters:apidb', 'ApiCommonDatasets:apidb',
                       'ReFlow:gus', 'ApiCommonModel:apidb','GBrowse:apidb',
                       'ApiCommonWebsite:apidb', 'ApiCommonWebService:apidb');

# Additional projects for Ortho
my @orthoSiteProjects = ('OrthoMCLModel:apidb', 'OrthoMCLWebsite:apidb',
                         'OrthoMCLWebService:apidb', 'DJob:gus');

# Additional projects for ClinEpi
my @clinEpiSiteProjects = ('ClinEpiPresenters:apidb', 'ClinEpiDatasets:apidb',
                           'ClinEpiModel:apidb', 'ClinEpiWebsite:apidb', 'ReFlow:gus');

# Additional projects for Microbiome
my @microbiomeSiteProjects = ('MicrobiomePresenters:apidb', 'MicrobiomeDatasets:apidb',
                              'MicrobiomeModel:apidb', 'MicrobiomeWebsite:apidb', 'ReFlow:gus');

# All other projects not listed above
my @otherProjects = ('DJob:gus', 'TuningManager:gus', 'OAuth2Server:gus',
                     'WDKTemplateSite:gus', 'WSFTemplate:gus', 'EuPathGalaxy:apidb',
                     'EuPathTemplateSite:apidb');

# Keys for selecting project groups
sub uniq { my %seen; grep !$seen{$_}++, @_; }
my %groupsMap = (
    'backend' => [uniq((@baseProjects, @backendProjects))],
    'apisite' => [uniq((@baseProjects, @sharedSiteProjects, @apiSiteProjects))],
    'orthosite' => [uniq((@baseProjects, @sharedSiteProjects, @orthoSiteProjects))],
    'clinepisite' => [uniq((@baseProjects, @sharedSiteProjects, @clinEpiSiteProjects))],
    'mbiosite' => [uniq((@baseProjects, @sharedSiteProjects, @microbiomeSiteProjects))],
    'allsite' => [uniq((@baseProjects, @sharedSiteProjects, @apiSiteProjects, @orthoSiteProjects,
                        @clinEpiSiteProjects, @microbiomeSiteProjects))],
    'all' => [uniq((@baseProjects, @sharedSiteProjects, @apiSiteProjects, @orthoSiteProjects,
                    @clinEpiSiteProjects, @microbiomeSiteProjects, @backendProjects, @otherProjects))]
);

sub getRepoBaseUrl {
    return $url;
}

sub getCmd {
    # get name of command that was run
    $0 =~ /(\w+)$/;
    return $1;
}

sub getProjectGroup {
    my $groupKey = $_[0];
    if (exists $groupsMap{$groupKey}) {
        my $arrayRef = $groupsMap{$groupKey};
        return @$arrayRef;
    }
    # bad project key; set error and return undefined
    $! = "Bad project group '$groupKey'; valid values are: " . join(', ', keys(%groupsMap));
    return undef;
}

sub getProjectGroupOptions {
    return '(' . join('|', keys(%groupsMap)) . ')';
}

sub printProjectGroupInfo {
    foreach my $key (keys %groupsMap) {
        print STDERR "    $key: ";
        my $arrayRef = $groupsMap{$key};
        foreach my $project (@$arrayRef) {
            my ($name, $repository) = split(/:/, $project);
            print STDERR "$name, ";
        }
        print STDERR "\n\n";
    }
}

1;
