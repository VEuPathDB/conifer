from __future__ import (absolute_import, division, print_function)
__metaclass__ = type

from ansible.plugins.callback import CallbackBase
from ansible import context
from copy import deepcopy

class CallbackModule(CallbackBase):

  '''
  This callback copies commandline extra vars into
  the conifer namespace dictionary.
  '''

  CALLBACK_VERSION = 2.0
  CALLBACK_NAME = 'copy_extra_vars'
  CALLBACK_NEEDS_ENABLED = True

  def __init__(self, display=None):
    super(CallbackModule, self).__init__()
    self._options = context.CLIARGS

  def v2_playbook_on_play_start(self, play):
    self.vm_extra_vars = play._variable_manager._extra_vars
    if self._options:
      dic = {'conifer': deepcopy(self.vm_extra_vars)}
      self.vm_extra_vars.update(dic)
