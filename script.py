#!/usr/bin/python3
import boto3
import datetime
import logging
import time
from decouple import config
from pythonjsonlogger import jsonlogger

runtime = config('RUNTIME')
region = boto3.session.Session().region_name
BY_RUNNING_STATE_CODE = {"Name": "instance-state-code", "Values": ["16"]}
BY_TAG_NAME = {"Name": "tag:k8s.io/role/master", "Values": ["1"]}

while True:
    # formatter construct
    class CustomJsonFormatter(jsonlogger.JsonFormatter):
        def add_fields(self, log_record, record, message_dict):
            log_record['line'] = record.message
            log_record['loglevel'] = record.levelname
            if not log_record.get('timestamp'):
                now = datetime.datetime.now().strftime("%d/%m/%Y %H:%M:%S")
                log_record['timestamp'] = now
            if not log_record.get('Running instances'):
                log_record['Running instances'] = int(len(instances_info) / 2)
            if not log_record.get('region'):
                log_record['region'] = region


    instances_info = {}


    def running_instances_info():
        i = 0
        ec2 = boto3.resource('ec2')
        running_instances = ec2.instances.filter(
            Filters=[BY_TAG_NAME, BY_RUNNING_STATE_CODE])
        for instance in running_instances:
            i += 1
            ip = instance.private_ip_address
            name = instance.state['Name']
            instances_info['instance' + str(i) + '_IP'] = ip
            instances_info['instance' + str(i) + '_Name'] = name


    formatter = CustomJsonFormatter('%(region)s  - %(timestamp)s  -  %(line)s - %(loglevel)s -%(Running instances)s')

    logHandler = logging.StreamHandler()
    logHandler.setFormatter(formatter)
    logger = logging.getLogger()
    logger.handlers.clear()
    logger.addHandler(logHandler)
    logger.setLevel(logging.INFO)
    running_instances_info()

    logger.info('Running instances for region:' + str(region) + '')

    time.sleep(int(runtime))
