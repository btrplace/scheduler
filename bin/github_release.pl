#!/usr/bin/perl -w
use strict;
use LWP::UserAgent;
use Env qw(GITHUB_TOKEN);

my $CHANGELOG = "CHANGES.md";
my $TAG_HEADER = "btrplace-scheduler-";
my $REPO = "btrplace/scheduler";
my $URL = "https://api.github.com/repos/$REPO/releases?access_token=$GITHUB_TOKEN";

if ($GITHUB_TOKEN eq "") {
	print("Environment variable GITHUB_TOKEN is missing");
	exit(1);
}

sub getLog  {
	my ($v) = @_;
	my $log = "";
	my $tracker = 0;
	open FP, $CHANGELOG or die ("Unable to open $CHANGELOG");
	my $in = 0;
	while (my $line = <FP>) {
		chomp $line;
		if ($line =~ /^version\ $v/) {
			$in = 1;
			<FP>
		} elsif ($line eq "") {
			$in = 0;
		} elsif ($in) {
			if ($line =~ /issue\stracker/) {
				$tracker = 1;
			}
			$log = $log . $line."\\n";
		}
	}
	close FP;
	if (!$tracker) {
		$log .= "- Refer to the associated [issue tracker](https://github.com/$REPO/issues?q=milestone%3A%22release+$v%22+is%3Aclosed) to get the closed issues"
	}
	return $log;
}

sub release {
	my ($v,$log) = @_;
	my $tag = "$TAG_HEADER$v";	
	$log =~ s/\'/\\'/g;	
	my $API_JSON='{"tag_name": "'.$tag.'","name": "'.$tag.'","body": "'.$log.'","draft": false,"prerelease": false}';	
	
	my $ua = LWP::UserAgent->new;
	# set custom HTTP request header fields
	my $req = HTTP::Request->new(POST => $URL);
	$req->header('content-type' => 'application/json');	
	# add POST data to HTTP request body	
	$req->content($API_JSON);
	print $API_JSON;
	my $resp = $ua->request($req);
	if ($resp->is_success) {
    	my $message = $resp->decoded_content;
    	print "Received reply: $message\n";
	} else {
    	print "HTTP POST error code: ", $resp->code, "\n";
    	print "HTTP POST error message: ", $resp->message, "\n";
	}
}

if (scalar(@ARGV) != 1) {
	print "Usage: changes.pl version";
	exit(1);
}

my $log = getLog($ARGV[0]);
release($ARGV[0], $log);


