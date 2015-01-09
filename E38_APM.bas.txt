rem Event 38 APM Tie-In Script
rem Free For Modification & Distribution
@title E38_APM
print "Script Started, Listening"
sleep 1000
goto "interval"
:interval
  p = get_usb_power
  if p > 0 then goto "picture"
  goto "interval"
:picture
  press "shoot_full"
  sleep 50
  release "shoot_full"
  goto "interval"
:terminate
  print "Shut-Down Command Received"
  sleep 1000
  shut_down